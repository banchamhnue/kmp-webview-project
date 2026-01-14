import SwiftUI
import WebKit
import shared

struct ContentView: View {
    private let apiClient = ApiClient()
    
    var body: some View {
        ZStack {
            WebView(url: URL(string: "https://sgcarmart.com")!)
                .edgesIgnoringSafeArea(.all)
        }
        .onAppear {
            fetchTodoData()
        }
    }
    
    func fetchTodoData() {
        Task {
            do {
                let todo = try await apiClient.fetchTodo()
                await MainActor.run {
                    PlatformKt.showMessage(message: todo.title)
                }
            } catch {
                await MainActor.run {
                    PlatformKt.showMessage(message: "Error: \(error.localizedDescription)")
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
