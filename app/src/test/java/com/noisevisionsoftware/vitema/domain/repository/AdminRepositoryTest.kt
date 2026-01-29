package com.noisevisionsoftware.vitema.domain.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.noisevisionsoftware.vitema.MainDispatcherRule
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.model.user.UserRole
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adminRepository: AdminRepository
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var query: Query

    private val testUser = User(
        id = "test_id",
        email = "test@example.com",
        nickname = "testUser",
        role = UserRole.USER,
        createdAt = 123456789
    )

    @Before
    fun setUp() {
        firestore = mockk()
        collectionReference = mockk()
        documentReference = mockk()
        query = mockk()

        every { firestore.collection("users") } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference

        adminRepository = AdminRepository(firestore)
    }

    @Test
    fun getAllUsersShouldReturnSuccessWithUsersList() = runTest {
        val documentSnapshot = mockk<DocumentSnapshot>()
        val querySnapshot = mockk<QuerySnapshot>()

        val createTask = Tasks.forResult(querySnapshot)
        every { collectionReference.get() } returns createTask
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.toObject(User::class.java) } returns testUser

        val result = adminRepository.getAllUsers()

        assertTrue(result.isSuccess)
        val users = result.getOrNull()
        assertNotNull(users)
        assertEquals(1, users?.size)
        assertEquals(testUser, users?.first())
    }

    @Test
    fun getAllUsersShouldReturnFailureWhenExceptionOccurs() = runTest {
        val exception = Exception("Test exception")
        every { collectionReference.get() } throws exception

        val result = adminRepository.getAllUsers()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun updateUserRoleShouldReturnFailureWhenUpdateFails() = runTest {
        val userId = "test_id"
        val newRole = UserRole.ADMIN
        val exception = Exception("Update failed")

        every {
            documentReference.update("role", newRole.name)
        } throws exception

        val result = adminRepository.updateUserRole(userId, newRole)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}