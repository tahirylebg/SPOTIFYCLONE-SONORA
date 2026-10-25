import { useMemo } from "react";
import type { Track } from "../api/types";
import { ArtistCard, QuickTile, TrackCard } from "../components/Cards";
import { EmptyState, PageHeader, Section } from "../components/Section";
import { TrackTable } from "../components/TrackTable";
import { greeting } from "../format";
import { useLibrary } from "../library/LibraryProvider";

export function HomePage() {
  const { tracks, artists, recent, playCount } = useLibrary();

  // Accès rapide : d'abord l'historique, complété par la bibliothèque.
  const quick = useMemo(() => {
    const seen = new Set<string>();
    return [...recent, ...tracks].filter((t) => !seen.has(t.id) && seen.add(t.id)).slice(0, 6);
  }, [recent, tracks]);

  const mostPlayed = useMemo<Track[]>(
    () => tracks.filter((t) => playCount(t.id) > 0).sort((a, b) => playCount(b.id) - playCount(a.id)).slice(0, 5),
    [tracks, playCount],
  );

  if (tracks.length === 0) {
    return (
      <>
        <PageHeader title={greeting()} />
        <EmptyState>Ta bibliothèque est vide. Ajoute des fichiers .mp3 ou .wav dans le dossier music/ puis relance le backend.</EmptyState>
      </>
    );
  }

  const recents = recent.slice(0, 6);
  return (
    <>
      <PageHeader title={greeting()} />
      <div className="quick-grid">
        {quick.map((t) => (
          <QuickTile key={t.id} track={t} context={quick} />
        ))}
      </div>

      {recents.length > 0 && (
        <Section title="Écoutés récemment">
          <div className="card-grid">
            {recents.map((t) => (
              <TrackCard key={t.id} track={t} context={recents} />
            ))}
          </div>
        </Section>
      )}

      <Section title="Tes artistes">
        <div className="card-grid">
          {artists.slice(0, 8).map((a) => (
            <ArtistCard key={a.name} artist={a} />
          ))}
        </div>
      </Section>

      {mostPlayed.length > 0 && (
        <Section title="Tes plus écoutés">
          <TrackTable tracks={mostPlayed} />
        </Section>
      )}
    </>
  );
}
