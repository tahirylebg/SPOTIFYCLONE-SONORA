import { useEffect, useRef } from "react";
import { Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { PlayerBar } from "./components/PlayerBar";
import { EmptyState } from "./components/Section";
import { MobileNav, Sidebar } from "./components/Sidebar";
import { useLibrary } from "./library/LibraryProvider";
import { DiscoverPage } from "./pages/DiscoverPage";
import { HomePage } from "./pages/HomePage";
import { LibraryPage } from "./pages/LibraryPage";
import { SearchPage } from "./pages/SearchPage";

export function App() {
  const { loading, error, reload } = useLibrary();
  const main = useRef<HTMLElement>(null);
  const location = useLocation();
  const navigate = useNavigate();

  // Chaque nouvelle page s'ouvre en haut.
  useEffect(() => {
    main.current?.scrollTo(0, 0);
  }, [location.pathname]);

  // Ctrl/⌘ + K ou "/" : aller à la recherche.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const typing = (e.target as HTMLElement).tagName === "INPUT";
      if (((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k") || (e.key === "/" && !typing)) {
        e.preventDefault();
        navigate("/recherche");
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [navigate]);

  return (
    <div className="app">
      <Sidebar />
      <main className="main" ref={main}>
        <div className="page">
          {error ? (
            <div className="error-state">
              <EmptyState>{error}</EmptyState>
              <button className="primary-btn" onClick={reload}>Réessayer</button>
            </div>
          ) : loading ? (
            <EmptyState>Chargement de ta bibliothèque…</EmptyState>
          ) : (
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/recherche" element={<SearchPage />} />
              <Route path="/bibliotheque" element={<LibraryPage />} />
              <Route path="/decouvertes" element={<DiscoverPage />} />
              <Route path="*" element={<EmptyState>Cette page n'existe pas.</EmptyState>} />
            </Routes>
          )}
        </div>
      </main>
      <PlayerBar />
      <MobileNav />
    </div>
  );
}
