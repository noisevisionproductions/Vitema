package com.noisevisionsoftware.szytadieta.domain.repository

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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalComposeApi
class AuthRepositoryTest {

    private lateinit var repository: AuthRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var email = "test@example.com"
    private var password = "password123"
    private var nickname = "testUser"
    private var userId = "testUserId"

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)

        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(TextUtils::class)

        every { FirebaseAuth.getInstance() } returns auth
        every { FirebaseFirestore.getInstance() } returns firestore
        every { TextUtils.isEmpty(any()) } returns false

/*
        repository = AuthRepository(auth, firestore)
*/
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
        val firebaseUser = mockk<FirebaseUser>()
        val authResult = mockk<AuthResult>()
        val documentReference = mockk<DocumentReference>(relaxed = true)

        every { firebaseUser.uid } returns userId
        every { authResult.user } returns firebaseUser

        val createUserTask = Tasks.forResult(authResult)
        every { auth.createUserWithEmailAndPassword(email, password) } returns createUserTask

        every { firestore.collection("users") } returns mockk {
            every { document(userId) } returns documentReference
        }

        val setUserTask: Task<Void> = Tasks.forResult(null)
        every { documentReference.set(any<User>()) } returns setUserTask

        val result =
            repository.register(nickname = nickname, email = email, password = password)

        assertThat(result.isSuccess).isTrue()
        val user = result.getOrNull()
        assertThat(user).isNotNull()
        assertThat(user?.email).isEqualTo(email)
        assertThat(user?.nickname).isEqualTo(nickname)
        assertThat(user?.id).isEqualTo(userId)

        verifySequence {
            auth.createUserWithEmailAndPassword(email, password)
            firestore.collection("users")
            documentReference.set(any<User>())
        }
    }

    @Test
    fun register_ShouldReturnFailure_WhenAuthCreationFails() = runTest {
        val exception = FirebaseAuthException("", "Registration failed")
        every { auth.createUserWithEmailAndPassword(email, password) } returns Tasks.forException(
            exception
        )

        val result = repository.register(nickname, email, password)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(FirebaseAuthException::class.java)
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