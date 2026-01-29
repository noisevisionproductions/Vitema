package com.noisevisionsoftware.vitema.domain.repository

import android.text.TextUtils
import androidx.compose.runtime.ExperimentalComposeApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.firestore.toObject
import com.noisevisionsoftware.vitema.data.FCMTokenRepository
import com.noisevisionsoftware.vitema.domain.model.user.Gender
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.model.user.pending.PendingMeasurement
import com.noisevisionsoftware.vitema.domain.model.user.pending.PendingUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalComposeApi
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fcmTokenRepository: FCMTokenRepository

    private var email = "test@example.com"
    private var password = "password123"
    private var nickname = "testUser"
    private var userId = "testUserId"

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        fcmTokenRepository = mockk(relaxed = true)

        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(TextUtils::class)
        mockkStatic(FCMTokenRepository::class)

        every { FirebaseAuth.getInstance() } returns auth
        every { FirebaseFirestore.getInstance() } returns firestore
        every { TextUtils.isEmpty(any()) } returns false

        repository = AuthRepository(auth, firestore, fcmTokenRepository)
    }

    @Test
    fun login_ShouldReturnSuccessWithUser_WhenLoginIsSuccessful() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val documentReference = mockk<DocumentReference>()
        val user = User(id = userId, email = email, nickname = nickname, createdAt = 123456789)

        every { firebaseUser.uid } returns userId
        every { authResult.user } returns firebaseUser
        every { documentSnapshot.toObject(User::class.java) } returns user

        every { auth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(
            authResult
        )
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.get() } returns Tasks.forResult(documentSnapshot)

        val result = repository.login(email, password)

        assertThat(result.isSuccess).isTrue()
        val loggedInUser = result.getOrNull()
        assertThat(loggedInUser).isEqualTo(user)

        verifySequence {
            auth.signInWithEmailAndPassword(email, password)
            firestore.collection("users")
            documentReference.get()
            documentSnapshot.toObject(User::class.java)
        }
    }

    @Test
    fun login_ShouldReturnFailure_WhenUserDataNotFound() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val documentReference = mockk<DocumentReference>()

        every { firebaseUser.uid } returns userId
        every { authResult.user } returns firebaseUser
        every { documentSnapshot.toObject(User::class.java) } returns null

        every { auth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(
            authResult
        )
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.get() } returns Tasks.forResult(documentSnapshot)

        val result = repository.login(email, password)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Nie znaleziono danych użytkownika")
    }

    @Test
    fun register_ShouldReturnSuccessWithUser_WhenRegistrationIsSuccessful() = runTest {
        // Mock dla dokumentu pendingUsers
        val pendingUserDocRef = mockk<DocumentReference>(relaxed = true)
        val pendingUserSnapshot = mockk<DocumentSnapshot>(relaxed = true)
        every { pendingUserSnapshot.exists() } returns false

        every { firestore.collection("pendingUsers") } returns mockk {
            every { document(email) } returns pendingUserDocRef
        }
        every { pendingUserDocRef.get() } returns Tasks.forResult(pendingUserSnapshot)

        // Mock dla autentykacji
        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        every { firebaseUser.uid } returns userId
        every { authResult.user } returns firebaseUser
        every { auth.createUserWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)

        // Mock dla zapisania użytkownika
        val usersCollectionRef = mockk<CollectionReference>()
        val userDocRef = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCollectionRef
        every { usersCollectionRef.document(userId) } returns userDocRef
        every { userDocRef.set(any<User>()) } returns Tasks.forResult(null)

        // Mock dla funkcji zawieszającej updateToken
        coEvery { fcmTokenRepository.updateToken(userId) } returns Result.success(Unit)

        // Wywołanie metody
        val result = repository.register(nickname = nickname, email = email, password = password)

        // Sprawdzenie wyniku
        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user).isNotNull()
        assertThat(user?.email).isEqualTo(email)
        assertThat(user?.nickname).isEqualTo(nickname)
        assertThat(user?.id).isEqualTo(userId)

        // Weryfikacja wywołań
        verify {
            firestore.collection("pendingUsers")
            pendingUserDocRef.get()
            auth.createUserWithEmailAndPassword(email, password)
            firestore.collection("users")
            userDocRef.set(any<User>())
        }

        // Weryfikacja wywołania funkcji zawieszającej
        coVerify { fcmTokenRepository.updateToken(userId) }
    }

    @Test
    fun register_ShouldReturnFailure_WhenAuthCreationFails() = runTest {
        // Mock dla dokumentu pendingUsers
        val pendingUserDocRef = mockk<DocumentReference>()
        val pendingUserSnapshot = mockk<DocumentSnapshot>()
        every { pendingUserSnapshot.exists() } returns false

        every { firestore.collection("pendingUsers") } returns mockk {
            every { document(email) } returns pendingUserDocRef
        }
        every { pendingUserDocRef.get() } returns Tasks.forResult(pendingUserSnapshot)

        // Symulacja błędu autentykacji
        val exception = FirebaseAuthException("ERROR_EMAIL_ALREADY_IN_USE", "Rejestracja nie powiodła się")
        every { auth.createUserWithEmailAndPassword(email, password) } returns Tasks.forException(exception)

        // Wywołanie metody
        val result = repository.register(nickname, email, password)

        // Sprawdzenie wyniku
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FirebaseAuthException::class.java)

        // Weryfikacja wywołań
        verify {
            firestore.collection("pendingUsers")
            pendingUserDocRef.get()
            auth.createUserWithEmailAndPassword(email, password)
        }

        // Weryfikacja braku innych wywołań
        verify(exactly = 0) {
            firestore.collection("users")
        }

        coVerify(exactly = 0) {
            fcmTokenRepository.updateToken(any())
        }
    }

    @Test
    fun register_ShouldMigratePendingUserData_WhenPendingUserExists() = runTest {
        // Mock dla dokumentu pendingUsers
        val pendingUserDocRef = mockk<DocumentReference>()
        val pendingUserSnapshot = mockk<DocumentSnapshot>()
        every { pendingUserSnapshot.exists() } returns true

        val pendingMeasurement = PendingMeasurement(
            date = 1615123200000,  // przykładowa data
            height = 180,
            weight = 75,
            neck = 38,
            biceps = 32,
            chest = 95,
            waist = 80,
            belt = 85,
            hips = 95,
            thigh = 55,
            calf = 38
        )

        val pendingUser = PendingUser(
            email = email,
            gender = Gender.MALE,
            age = 30,
            firstAndLastName = "Jan Kowalski",
            lastUpdated = Date(),
            measurements = listOf(pendingMeasurement)
        )

        every { firestore.collection("pendingUsers") } returns mockk<CollectionReference> {
            every { document(email) } returns pendingUserDocRef
        }

        // Symulujemy poprawne pobranie pendingUser
        val pendingUserTask = mockk<Task<DocumentSnapshot>> {
            every { isComplete } returns true
            every { exception } returns null
            every { isCanceled } returns false
            every { isSuccessful } returns true
            every { result } returns pendingUserSnapshot  // Dodajemy mockowanie result
            every { addOnCompleteListener(any()) } returns this
            every { addOnSuccessListener(any()) } returns this
            every { addOnFailureListener(any()) } returns this
        }
        every { pendingUserDocRef.get() } returns pendingUserTask
        every { pendingUserSnapshot.toObject<PendingUser>() } returns pendingUser

        // Symulujemy poprawne usunięcie pendingUser
        val deleteTask = mockk<Task<Void>> {
            every { isComplete } returns true
            every { exception } returns null
            every { isCanceled } returns false
            every { isSuccessful } returns true
            every { result } returns null  // Dodajemy mockowanie result
            every { addOnCompleteListener(any()) } returns this
            every { addOnSuccessListener(any()) } returns this
            every { addOnFailureListener(any()) } returns this
        }
        every { pendingUserDocRef.delete() } returns deleteTask

        // Mock dla autentykacji
        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        every { firebaseUser.uid } returns userId
        every { authResult.user } returns firebaseUser
        val authTask = mockk<Task<AuthResult>> {
            every { isComplete } returns true
            every { exception } returns null
            every { isCanceled } returns false
            every { isSuccessful } returns true
            every { result } returns authResult  // Dodajemy mockowanie result
            every { addOnCompleteListener(any()) } returns this
            every { addOnSuccessListener(any()) } returns this
            every { addOnFailureListener(any()) } returns this
        }
        every { auth.createUserWithEmailAndPassword(email, password) } returns authTask

        // Mock dla zapisania pomiarów
        val measurementsCollectionRef = mockk<CollectionReference>()
        val measurementDocRef = mockk<DocumentReference>()
        every { firestore.collection("bodyMeasurements") } returns measurementsCollectionRef
        every { measurementsCollectionRef.document(any()) } returns measurementDocRef

        // Symulujemy poprawne zapisanie pomiaru
        val setMeasurementTask = mockk<Task<Void>> {
            every { isComplete } returns true
            every { exception } returns null
            every { isCanceled } returns false
            every { isSuccessful } returns true
            every { result } returns null  // Dodajemy mockowanie result
            every { addOnCompleteListener(any()) } returns this
            every { addOnSuccessListener(any()) } returns this
            every { addOnFailureListener(any()) } returns this
        }
        every { measurementDocRef.set(any()) } returns setMeasurementTask

        // Mock dla zapisania użytkownika
        val usersCollectionRef = mockk<CollectionReference>()
        val userDocRef = mockk<DocumentReference>()
        every { firestore.collection("users") } returns usersCollectionRef
        every { usersCollectionRef.document(userId) } returns userDocRef

        // Symulujemy poprawne zapisanie użytkownika
        val setUserTask = mockk<Task<Void>> {
            every { isComplete } returns true
            every { exception } returns null
            every { isCanceled } returns false
            every { isSuccessful } returns true
            every { result } returns null  // Dodajemy mockowanie result
            every { addOnCompleteListener(any()) } returns this
            every { addOnSuccessListener(any()) } returns this
            every { addOnFailureListener(any()) } returns this
        }
        every { userDocRef.set(any<User>()) } returns setUserTask

        // Mock dla funkcji zawieszającej updateToken
        coEvery { fcmTokenRepository.updateToken(userId) } returns Result.success(Unit)

        // Wywołanie metody
        val result = repository.register(nickname = nickname, email = email, password = password)

        // Dodajemy logowanie w przypadku błędu
        if (result.isFailure) {
            println("Test failed with exception: ${result.exceptionOrNull()}")
            result.exceptionOrNull()?.printStackTrace()
        }

        // Sprawdzenie wyniku
        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user).isNotNull()
        assertThat(user?.email).isEqualTo(email)
        assertThat(user?.storedAge).isEqualTo(30)
        assertThat(user?.gender).isEqualTo(Gender.MALE)
        assertThat(user?.profileCompleted).isTrue()

        // Weryfikacja wywołań - używamy verify zamiast verifySequence
        verify {
            firestore.collection("pendingUsers")
            pendingUserDocRef.get()
            pendingUserSnapshot.toObject<PendingUser>()
            firestore.collection("bodyMeasurements")
            measurementDocRef.set(any())
            pendingUserDocRef.delete()
            firestore.collection("users")
            userDocRef.set(any<User>())
        }

        // Weryfikacja wywołania funkcji zawieszającej
        coVerify { fcmTokenRepository.updateToken(userId) }
    }

    @Test
    fun getCurrentUserData_ShouldReturnSuccess_WhenUserIsLoggedIn() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val documentReference = mockk<DocumentReference>()
        val user = User(id = userId, email = email, nickname = nickname, createdAt = 123456789)

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userId
        every { documentSnapshot.toObject(User::class.java) } returns user

        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.get() } returns Tasks.forResult(documentSnapshot)

        val result = repository.getCurrentUserData()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(user)
    }

    @Test
    fun getCurrentUserData_ShouldReturnSuccessNull_WhenNoUserLoggedIn() = runTest {
        every { auth.currentUser } returns null

        val result = repository.getCurrentUserData()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun updateUserData_ShouldReturnSuccess_WhenUpdateSucceeds() = runTest {
        val documentReference = mockk<DocumentReference>()
        val user = User(id = userId, email = email, nickname = nickname, createdAt = 123456789)

        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.set(user) } returns Tasks.forResult(null)

        val result = repository.updateUserData(user)

        assertThat(result.isSuccess).isTrue()

        verifySequence {
            firestore.collection("users")
            documentReference.set(user)
        }
    }

    @Test
    fun resetPassword_ShouldReturnSuccess_WhenResetSucceeds() = runTest {
        every { auth.sendPasswordResetEmail(email) } returns Tasks.forResult(null)

        val result = repository.resetPassword(email)

        assertThat(result.isSuccess).isTrue()
        verify { auth.sendPasswordResetEmail(email) }
    }

    @Test
    fun resetPassword_ShouldReturnFailure_WhenResetFails() = runTest {
        val exception = FirebaseAuthException("", "Reset failed")
        every { auth.sendPasswordResetEmail(email) } returns Tasks.forException(exception)

        val result = repository.resetPassword(email)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FirebaseAuthException::class.java)
    }

    @Test
    fun updatePassword_ShouldReturnSuccess_WhenCredentialsAreValid() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val credential = mockk<AuthCredential>()

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.email } returns email
        mockkStatic(EmailAuthProvider::class)
        every { EmailAuthProvider.getCredential(email, "oldPassword") } returns credential
        every { firebaseUser.reauthenticate(credential) } returns Tasks.forResult(mockk())
        every { firebaseUser.updatePassword("newPassword") } returns Tasks.forResult(null)

        val result = repository.updatePassword("oldPassword", "newPassword")

        assertThat(result.isSuccess).isTrue()
        verifySequence {
            auth.currentUser
            firebaseUser.email
            EmailAuthProvider.getCredential(email, "oldPassword")
            firebaseUser.reauthenticate(credential)
            firebaseUser.updatePassword("newPassword")
        }
    }

    @Test
    fun updatePassword_ShouldReturnFailure_WhenUserNotLoggedIn() = runTest {
        every { auth.currentUser } returns null

        val result = repository.updatePassword("oldPassword", "newPassword")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Użytkownik nie jest zalogowany")
    }

    @Test
    fun updatePassword_ShouldReturnFailure_WhenEmailIsMissing() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.email } returns null

        val result = repository.updatePassword("oldPassword", "newPassword")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Brak adresu email")
    }

    @Test
    fun getCurrentUser_ShouldReturnCurrentUser() {
        val firebaseUser = mockk<FirebaseUser>()
        every { auth.currentUser } returns firebaseUser

        val result = repository.getCurrentUser()

        assertThat(result).isEqualTo(firebaseUser)
    }

    @Test
    fun logout_ShouldSignOutUser() = runTest {
        repository.logout()

        verify { auth.signOut() }
    }

    @Test
    fun deleteAccount_ShouldReturnSuccess_WhenDeletionIsSuccessful() = runTest {
        val firebaseUser = mockk<FirebaseUser>()
        val documentReference = mockk<DocumentReference>()
        val querySnapshot = mockk<QuerySnapshot>()
        val batch = mockk<WriteBatch>()
        val documents = listOf(
            mockk<DocumentSnapshot> {
                every { reference } returns mockk()
            }
        )

        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userId
        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }
        every { documentReference.delete() } returns Tasks.forResult(null)
        every { firestore.batch() } returns batch
        every { firestore.collection("bodyMeasurements") } returns mockk {
            every { whereEqualTo("userId", userId) } returns mockk {
                every { get() } returns Tasks.forResult(querySnapshot)
            }
        }
        every { querySnapshot.documents } returns documents
        every { batch.delete(any()) } returns batch
        every { batch.commit() } returns Tasks.forResult(null)
        every { firebaseUser.delete() } returns Tasks.forResult(null)

        val result = repository.deleteAccount()

        assertThat(result.isSuccess).isTrue()
        verify{
            documentReference.delete()
            batch.delete(any())
            batch.commit()
            firebaseUser.delete()
        }
    }
    @Test
    fun deleteAccount_ShouldReturnFailure_WhenUserNotLoggedIn() = runTest {
        every { auth.currentUser }returns null

        val result = repository.deleteAccount()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Użytkownik nie jest zalogowany")
    }
}