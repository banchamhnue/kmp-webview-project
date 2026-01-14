import SwiftUI
import WebKit
import shared

struct ContentView: View {
    @StateObject private var viewModel = TodoViewModel()
    
    var body: some View {
        ZStack {
            WebView(url: URL(string: "https://sgcarmart.com")!)
                .edgesIgnoringSafeArea(.all)
        }
        .onAppear {
            viewModel.fetchTodo()
        }
        .onChange(of: viewModel.todoState) { state in
            handleTodoState(state)
        }
    }
    
    func handleTodoState(_ state: TodoViewState) {
        switch state {
        case .success(let todo):
            PlatformKt.showMessage(message: todo.title)
        case .error(let message):
            PlatformKt.showMessage(message: "Error: \(message)")
        case .loading:
            break
        }
    }
}

enum TodoViewState: Equatable {
    case loading
    case success(Todo)
    case error(String)
}

class TodoViewModel: ObservableObject {
    @Published var todoState: TodoViewState = .loading
    private let getTodoUseCase: GetTodoUseCase
    
    init() {
        self.getTodoUseCase = CommonModule.shared.provideGetTodoUseCase()
    }
    
    func fetchTodo() {
        todoState = .loading
        Task {
            do {
                let result = try await getTodoUseCase.invoke()
                if let todo = result.getOrNull() {
                    await MainActor.run {
                        todoState = .success(todo)
                    }
                } else if let error = result.exceptionOrNull() {
                    await MainActor.run {
                        todoState = .error(error.localizedDescription)
                    }
                }
            } catch {
                await MainActor.run {
                    todoState = .error(error.localizedDescription)
                }
            }
        }
    }
}

struct WebView: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        let request = URLRequest(url: url)
        webView.load(request)
    }
}
