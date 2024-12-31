package com.noisevisionsoftware.szytadieta.domain.repository

import android.icu.util.Calendar
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BodyMeasurementRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: BodyMeasurementRepository
    private lateinit var collectionReference: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var query: Query

    private val testMeasurement = BodyMeasurements(
        id = "test_id",
        userId = "user_id",
        date = System.currentTimeMillis(),
        neck = 35.0,
        biceps = 32.0,
        chest = 95.0,
        waist = 80.0,
        hips = 90.0,
        thigh = 55.0,
        calf = 35.0,
        weight = 75.0,
        weekNumber = 1
    )


    @Before
    fun setUp() {
        firestore = mockk(relaxed = true)
        collectionReference = mockk(relaxed = true)
        documentReference = mockk(relaxed = true)
        query = mockk(relaxed = true)

        every { firestore.collection(any()) } returns collectionReference
        every { collectionReference.document(any()) } returns documentReference
        every { collectionReference.whereEqualTo(any<String>(), any()) } returns query
        every { query.whereEqualTo(any<String>(), any()) } returns query
        every { query.whereGreaterThanOrEqualTo(any<String>(), any()) } returns query
        every { query.whereLessThanOrEqualTo(any<String>(), any()) } returns query
        every { query.orderBy(any<String>(), any()) } returns query

        repository = BodyMeasurementRepository(firestore)
    }

    @Test
    fun addMeasurements_ShouldReturnSuccess_WhenOperationSucceeds() = runTest {
        val successTask = Tasks.forResult<Void?>(null)
        every { documentReference.set(any()) } returns successTask

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.addMeasurements(testMeasurement)
        }

        assertThat(result.isSuccess).isTrue()
        verify {
            firestore.collection("bodyMeasurements")
            documentReference.set(testMeasurement)
        }
    }

    @Test
    fun addMeasurements_ShouldReturnFailure_WhenOperationFails() = runTest {
        val exception = Exception("Failed to add measurement")
        coEvery { documentReference.set(any()) } throws exception

        val result = repository.addMeasurements(testMeasurement)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun getMeasurementsHistory_ShouldReturnMeasurements_WhenNoDateRange() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshot = mockk<DocumentSnapshot>()

        coEvery { query.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.toObject(BodyMeasurements::class.java) } returns testMeasurement

        val result = repository.getMeasurementsHistory("user_id")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).hasSize(1)
        assertThat(result.getOrNull()?.first()).isEqualTo(testMeasurement)
        verify {
            collectionReference.whereEqualTo("userId", "user_id")
            query.orderBy("date", Query.Direction.DESCENDING)
        }
    }

    @Test
    fun getMeasurementsHistory_ShouldReturnMeasurements_WhenNoDateRangeProvided() = runTest {
        val startDate = 1000L
        val endDate = 2000L
        val querySnapshot = mockk<QuerySnapshot>()

        coEvery { query.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns emptyList()

        val result = repository.getMeasurementsHistory("user_id", startDate, endDate)

        assertThat(result.isSuccess).isTrue()
        verify {
            query.whereGreaterThanOrEqualTo("date", startDate)
            query.whereLessThanOrEqualTo("date", endDate)
        }
    }

    @Test
    fun getCurrentWeekMeasurements_ShouldReturnSuccess_WhenMeasurementExists() = runTest {
        mockkStatic(Calendar::class)
        val calendar = mockk<Calendar>()
        val currentWeek = 25

        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.WEEK_OF_YEAR) } returns currentWeek

        val querySnapshot = mockk<QuerySnapshot>()
        val documentSnapshot = mockk<DocumentSnapshot>()

        coEvery { query.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns listOf(documentSnapshot)
        every { documentSnapshot.toObject(BodyMeasurements::class.java) } returns testMeasurement

        val result = repository.getCurrentWeekMeasurements("user_id")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(testMeasurement)
        verify {
            collectionReference.whereEqualTo("userId", "user_id")
            query.whereEqualTo("weekNumber", currentWeek)
        }
    }

    @Test
    fun getCurrentWeekMeasurements_ShouldReturnSuccessWitHNull_WhenNoMeasurementExists() = runTest {
        mockkStatic(Calendar::class)
        val calendar = mockk<Calendar>()
        val currentWeek = 25
        every { Calendar.getInstance() } returns calendar
        every { calendar.get(Calendar.WEEK_OF_YEAR) } returns currentWeek

        val querySnapshot = mockk<QuerySnapshot>()
        val successTask = Tasks.forResult(querySnapshot)
        every { query.get() } returns successTask
        every { querySnapshot.documents } returns emptyList()

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.getCurrentWeekMeasurements("user_id")
        }

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
        verify {
            collectionReference.whereEqualTo("userId", "user_id")
            query.whereEqualTo("weekNumber", currentWeek)
        }

        unmockkStatic(Calendar::class)
    }

    @Test
    fun deleteMeasurement_ShouldReturnSuccess_WhenOperationSucceeds() = runTest {
        val successTask = Tasks.forResult<Void?>(null)
        every { documentReference.delete() } returns successTask

        val result = withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.deleteMeasurement("measurement_id")
        }

        assertThat(result.isSuccess).isTrue()
        verify {
            firestore.collection("bodyMeasurements")
            documentReference.delete()
        }
    }

    @Test
    fun deleteMeasurement_ShouldReturnFailure_WhenOperationFails() = runTest {
        val exception = Exception("Failed to delete measurement")
        coEvery { documentReference.delete() } throws exception

        val result = repository.deleteMeasurement("measurement_id")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}