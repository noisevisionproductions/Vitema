package com.noisevisionsoftware.szytadieta.di.worker

import com.noisevisionsoftware.szytadieta.domain.repository.meals.SyncWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerBindingModule {

    @Binds
    @IntoMap
    @WorkerKey(SyncWorker::class)
    abstract fun bindSyncWorker(factory: SyncWorker.Factory): ChildWorkerFactory
}
