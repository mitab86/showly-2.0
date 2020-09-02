package com.michaldrabik.showly2.repository.shows

import BaseMockTest
import com.google.common.truth.Truth.assertThat
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.storage.database.dao.SeeLaterShowsDao
import com.michaldrabik.storage.database.model.SeeLaterShow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.michaldrabik.storage.database.model.Show as ShowDb

class SeeLaterShowsRepositoryTest : BaseMockTest() {

  @MockK lateinit var seeLaterShowsDao: SeeLaterShowsDao
  @RelaxedMockK lateinit var showDb: ShowDb

  private lateinit var SUT: SeeLaterShowsRepository

  @Before
  override fun setUp() {
    super.setUp()
    SUT = SeeLaterShowsRepository(database, mappers)

    coEvery { database.seeLaterShowsDao() } returns seeLaterShowsDao
  }

  @After
  fun confirmSutVerified() {
    confirmVerified(seeLaterShowsDao)
  }

  @Test
  fun `Should load and map all SeeLater shows`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAll() } returns listOf(showDb)
      coEvery { mappers.show.fromDatabase(any()) } returns Show.EMPTY

      SUT.loadAll()

      coVerify(exactly = 1) { seeLaterShowsDao.getAll() }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should load and map single SeeLater show by Trakt ID`() {
    runBlocking {
      val show = Show.EMPTY.copy(title = "Test")

      coEvery { seeLaterShowsDao.getById(any()) } returns showDb
      coEvery { mappers.show.fromDatabase(any()) } returns show

      val testShow = SUT.load(IdTrakt(1L))

      assertThat(testShow?.title).isEqualTo(show.title)
      coVerify(exactly = 1) { seeLaterShowsDao.getById(any()) }
      coVerify(exactly = 1) { mappers.show.fromDatabase(showDb) }
    }
  }

  @Test
  fun `Should insert show into database using Trakt ID`() {
    runBlocking {
      val slot = slot<SeeLaterShow>()
      coJustRun { seeLaterShowsDao.insert(capture(slot)) }

      SUT.insert(IdTrakt(1L))

      assertThat(slot.captured.id).isEqualTo(0)
      assertThat(slot.captured.idTrakt).isEqualTo(1)

      coVerify(exactly = 1) { seeLaterShowsDao.insert(any()) }
    }
  }

  @Test
  fun `Should delete show from database using Trakt ID`() {
    runBlocking {
      val slot = slot<Long>()
      coJustRun { seeLaterShowsDao.deleteById(capture(slot)) }

      SUT.delete(IdTrakt(10L))

      assertThat(slot.captured).isEqualTo(10L)
      coVerify(exactly = 1) { seeLaterShowsDao.deleteById(10L) }
    }
  }

  @Test
  fun `Should load all SeeLater shows ids`() {
    runBlocking {
      coEvery { seeLaterShowsDao.getAllTraktIds() } returns listOf(1L, 2L)

      val ids = SUT.loadAllIds()

      assertThat(ids).containsExactly(1L, 2L)
      coVerify(exactly = 1) { seeLaterShowsDao.getAllTraktIds() }
    }
  }
}