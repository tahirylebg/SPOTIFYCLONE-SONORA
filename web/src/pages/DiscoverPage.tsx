import { useMemo, useState } from "react";
import type { Track } from "../api/types";
import { TrackCard } from "../components/Cards";
import { Cover } from "../components/Cover";
import { Icon } from "../components/Icon";
import { EmptyState, PageHeader, Section } from "../components/Section";
import { useLibrary } from "../library/LibraryProvider";
import { shuffleInPlace } from "../player/PlaybackQueue";
import { usePlayer } from "../player/PlayerProvider";

const SELECTION_SIZE = 6;

export function DiscoverPage() {
  const { tracks, playCount } = useLibrary();
  const { playShuffled } = usePlayer();
  // Seul le bouton "Mélanger" renouvelle la sélection (pas chaque nouvelle écoute).
  const [seed, setSeed] = useState(0);

  // Dépend de `seed` (bouton) et de `tracks`, mais pas de l'historique.
  const random = useMemo(() => shuffleInPlace([...tracks]).slice(0, SELECTION_SIZE), [tracks, seed]);
  const preview = useMemo(() => shuffleInPlace([...tracks]).slice(0, 3), [tracks]);
  const neverPlayed = tracks.filter((t) => playCount(t.id) === 0).slice(0, SELECTION_SIZE);

  if (tracks.length === 0) {
    return (
      <>
        <PageHeader title="Découvertes" />
        <EmptyState>Ta bibliothèque est vide.</EmptyState>
      </>
    );
  }

  return (
    <>
      <PageHeader title="Découvertes" subtitle="Laisse-toi surprendre par ta propre bibliothèque." />

      <div className="hero">
        <div className="hero-text-block">
          <span className="hero-kicker">Mix</span>
          <h2 className="hero-title">Mix découverte</h2>
          <p className="hero-text">Toute ta bibliothèque, dans un ordre que tu n'as jamais entendu.</p>
          <button className="primary-btn" onClick={() => playShuffled(tracks)}>
            <Icon name="play" size={16} /> Lancer le mix
          </button>
        </div>
        <div className="hero-covers">
          {preview.map((t) => (
            <Cover key={t.id} url={t.coverUrl} label={t.title} size={120} />
          ))}
        </div>
      </div>

      <Section title="Jamais écoutés">
        {neverPlayed.length === 0 ? (
          <EmptyState>Tu as écouté chaque titre au moins une fois. Bien joué !</EmptyState>
        ) : (
          <CardGrid tracks={neverPlayed} />
        )}
      </Section>

      <Section
        title="Au hasard"
        action={
          <button className="ghost-btn" onClick={() => setSeed((s) => s + 1)}>
            <Icon name="shuffle" size={14} /> Mélanger
          </button>
        }
      >
        <CardGrid tracks={random} />
      </Section>
    </>
  );
}

function CardGrid({ tracks }: { tracks: Track[] }) {
  return (
    <div className="card-grid">
      {tracks.map((t) => (
        <TrackCard key={t.id} track={t} context={tracks} />
      ))}
    </div>
  );
}
