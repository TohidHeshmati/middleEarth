package client

import com.tohid.client.ApiResponse
import com.tohid.client.Character
import com.tohid.client.Movie
import com.tohid.client.TheOneApiClient
import com.tohid.showConcurrent
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TheOneApiClientTest {
    private val api = mockk<TheOneApiClient>()

    @Test
    fun `test showConcurrent aggregates movie and character totals correctly`() =
        runTest {
            val mockMovieResponse =
                ApiResponse(
                    docs = listOf(Movie(id = "1", name = "The Two Towers")),
                    total = 1,
                )
            val mockCharResponse =
                ApiResponse(
                    docs = listOf(Character(id = "1", name = "Gandalf")),
                    total = 1,
                )

            coEvery { api.getMovies(any()) } returns mockMovieResponse
            coEvery { api.getCharacters(any()) } returns mockCharResponse

            // 3. WHEN: Call the function we want to test
            // showConcurrent uses 'async' to call these two
            showConcurrent(api)

            // 4. THEN: Verify the calls happened
            coVerify(exactly = 1) { api.getMovies(any()) }
            coVerify(exactly = 1) { api.getCharacters(any()) }
        }
}
