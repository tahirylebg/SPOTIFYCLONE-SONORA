import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import type { Artist, SearchResult } from "../api/types";
import { ArtistCard, PlayButton } from "../components/Cards";
import { Cover } from "../components/Cover";
import { Icon } from "../components/Icon";
import { EmptyState, PageHeader, Section } from "../components/Section";
import { TrackTable } from "../components/TrackTable";
import { colorFor } from "../format";
import { useLibrary } from "../library/LibraryProvider";
import { usePlayer } from "../player/PlayerProvider";

export function SearchPage() {
  const [params, setParams] = useSearchParams();
  const query = params.get("q") ?? "";
  const [result, setResult] = useState<SearchResult | null>(null);
  const input = useRef<HTMLInputElement>(null);
  const { artists } = useLibrary();

  useEffect(() => {
    input.current?.focus();
  }, []);

  // Recherche côté API, légèrement différée pour ne pas appeler le serveur à chaque touche.
  useEffect(() => {
    if (!query.trim()) {
      setResult(null);
      return;
    }
    const controller = new AbortController();
    const timer = setTimeout(() => {
      api.search(query, controller.signal).then(setResult).catch(() => {});
    }, 150);
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query]);

  const setQuery = (q: string) => setParams(q ? { q } : {}, { replace: true });

  return (
    <>
      <PageHeader title="Rechercher" />
      <label className="search-box search-box-large">
        <Icon name="search" size={20} />
        <input
          ref={input}
          className="search-field"
          placeholder="Que veux-tu écouter ?"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Escape" && setQuery("")}
        />
      </label>

      {!query.trim() ? (
        <Section title="Parcourir tes artistes">
          <div className="browse-grid">
            {artists.map((a) => (
              <BrowseTile key={a.name} artist={a} onOpen={() => setQuery(a.name)} />
            ))}
          </div>
        </Section>
      ) : result && <Results query={query} result={result} />}
    </>
  );
}

function Results({ query, result }: { query: string; result: SearchResult }) {
  const { play } = usePlayer();
  const { tracks, artists } = result;

  if (tracks.length === 0 && artists.length === 0) {
    return (
      <EmptyState>
        Aucun résultat pour « {query.trim()} ».
        <br />
        Vérifie l'orthographe ou essaie un autre mot-clé.
      </EmptyState>
    );
  }

  const best = tracks[0];
  return (
    <>
      {best && (
        <div className="search-top">
          <Section title="Meilleur résultat">
            <div className="best-result" onDoubleClick={() => play(best, tracks)}>
              <Cover url={best.coverUrl} label={best.title} size={92} />
              <div className="best-title">{best.title}</div>
              <div className="best-meta">
                <span className="best-artist">{best.artist}</span>
                <span className="tag">Titre</span>
              </div>
              <PlayButton onPlay={() => play(best, tracks)} size={48} label={`Lire ${best.title}`} />
            </div>
          </Section>
          <Section title="Titres">
            <TrackTable tracks={tracks.slice(0, 5)} />
          </Section>
        </div>
      )}
      {artists.length > 0 && (
        <Section title="Artistes">
          <div className="card-grid">
            {artists.map((a) => (
              <ArtistCard key={a.name} artist={a} />
            ))}
          </div>
        </Section>
      )}
    </>
  );
}

/** Tuile colorée : nom en haut à gauche, pochette inclinée en bas à droite. */
function BrowseTile({ artist, onOpen }: { artist: Artist; onOpen: () => void }) {
  return (
    <button className="browse-tile" style={{ background: colorFor(artist.name) }} onClick={onOpen}>
      <span className="browse-title">{artist.name}</span>
      <span className="browse-cover">
        <Cover url={artist.coverUrl} label={artist.name} size={84} />
      </span>
    </button>
  );
}
