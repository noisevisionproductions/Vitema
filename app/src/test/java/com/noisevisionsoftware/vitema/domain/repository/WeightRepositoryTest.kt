package com.noisevisionsoftware.vitema.domain.repository

import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.noisevisionsoftware.vitema.MainDispatcherRule
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.vitema.domain.repository.health.WeightRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeightRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: WeightRepository
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var query: Query

    private val testMeasurement = BodyMeasurements(
        id = "test_id",
        userId = "user_id",
        date = System.currentTimeMillis(),
        weight = 75,
        measurementType = MeasurementType.WEIGHT_ONLY
    )

    @Before
    fun setUp() {
        firestore = mockk()
        collectionReference = mockk()
        documentReference = mockk()
        query = mockk()

        every { firestore.collection(any()) } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { collectionReference.whereEqualTo(any<String>(), any()) } returns query
        every { query.orderBy(any<String>(), any()) } returns query

/*
        repository = WeightRepository(firestore)
*/
    }

    @Test
    fun addWeight_ShouldReturnSuccess_WhenOperationSucceeds() = runTest {
        val successTask = Tasks.forResult<Void?>(null)
        every { documentReference.set(any()) } returns successTask

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.addWeight(testMeasurement)
        }

        assertThat(result.isSuccess).isTrue()
        verify {
            firestore.collection("bodyMeasurements")
            documentReference.set(testMeasurement)
        }
    }

    @Test
    fun addWeight_ShouldReturnFailure_WhenOperationFails() = runTest {
        val exception = Exception("Failed to add weight")
        every { documentReference.set(any()) } throws exception

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.addWeight(testMeasurement)
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun getUserWeights_ShouldReturnSuccess_WithMeasurements() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        val successTask = Tasks.forResult(querySnapshot)

        every { query.get() } returns successTask
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.toObject(BodyMeasurements::class.java) } returns testMeasurement

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.getUserWeights("user_id")
        }

        assertThat(result.isSuccess).isTrue()
        val weights = result.getOrNull()
        assertThat(weights).isNotNull()
        assertThat(weights).hasSize(1)
        assertThat(weights?.first()).isEqualTo(testMeasurement)
        verify {
            collectionReference.whereEqualTo("userId", "user_id")
            query.orderBy("date", Query.Direction.DESCENDING)
        }
    }

    @Test
    fun getUserWeights_ShouldReturnSuccessWithEmptyList_WhenNoWeightsExist() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        val successTask = Tasks.forResult(querySnapshot)

        every { query.get() } returns successTask
        every { querySnapshot.documents } returns emptyList()

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.getUserWeights("user_id")
        }

        assertThat(result.isSuccess).isTrue()
        val weights = result.getOrNull()
        assertThat(weights).isNotNull()
        assertThat(weights).isEmpty()
    }

    @Test
    fun getUserWeights_ShouldReturnFailure_WhenOperationFails() = runTest {
        val exception = Exception("Failed to get weights")
        every { query.get() } throws exception

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.getUserWeights("user_id")
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun deleteWeight_ShouldReturnSuccess_WhenOperationSucceeds() = runTest {
        val successTask = Tasks.forResult<Void?>(null)
        every { documentReference.delete() } returns successTask

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.deleteWeight("weight_id")
        }

        assertThat(result.isSuccess).isTrue()
        verify {
            firestore.collection("bodyMeasurements")
            documentReference.delete()
        }
    }

    @Test
    fun deleteWeight_ShouldReturnFailure_WhenOperationFails() = runTest {
        val exception = Exception("Failed to delete weight")
        every { documentReference.delete() } throws exception

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.deleteWeight("weight_id")
        }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}