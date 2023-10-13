import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

data class BirdsUiState(
    val currentCategory: String? = null,
    val images: List<BirdImage> = emptyList(),
) {
    val categories: HashSet<String> = images.map { it.category }.toHashSet()

    val categoryImages: List<BirdImage> = images.filter { it.category == currentCategory }
}

class BirdsViewModel: ViewModel() {
    private val _uiState: MutableStateFlow<BirdsUiState> = MutableStateFlow(BirdsUiState())
    val uiState: StateFlow<BirdsUiState> = _uiState.asStateFlow()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        updateImages()
    }

    override fun onCleared() {
        httpClient.close()
    }

    fun updateImages() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(images = getImages())
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(currentCategory = category)
        }
    }

    private suspend fun getImages(): List<BirdImage> {
        val images = httpClient.get("https://sebastianaigner.github.io/demo-image-api/pictures.json")
            .body<List<BirdImage>>()
        return images
    }
}