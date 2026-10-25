import { useMemo, useState } from "react";
import type { Track } from "../api/types";
import { ArtistCard } from "../components/Cards";
import { EmptyState, PageHeader } from "../components/Section";
import { TrackTable } from "../components/TrackTable";
import { plural, summary } from "../format";
import { useLibrary } from "../library/LibraryProvider";

type View = "titres" | "artistes";
type Sort = "defaut" | "titre" | "artiste" | "duree";

const SORTS: { value: Sort; label: string; compare?: (a: Track, b: Track) => number }[] = [
  { value: "defaut", label: "Par défaut" },
  { value: "titre", label: "Titre", compare: (a, b) => a.title.localeCompare(b.title, "fr") },
  { value: "artiste", label: "Artiste", compare: (a, b) => a.artist.localeCompare(b.artist, "fr") || a.title.localeCompare(b.title, "fr") },
  { value: "duree", label: "Durée", compare: (a, b) => b.durationSeconds - a.durationSeconds },
];

export function LibraryPage() {
  const { tracks, artists } = useLibrary();
  const [view, setView] = useState<View>("titres");
  const [sort, setSort] = useState<Sort>("defaut");

  const sorted = useMemo(() => {
    const compare = SORTS.find((s) => s.value === sort)?.compare;
    return compare ? [...tracks].sort(compare) : tracks;
  }, [tracks, sort]);

  return (
    <>
      <PageHeader title="Bibliothèque" subtitle={`${summary(tracks)} · ${plural(artists.length, "artiste")}`} />
      {tracks.length === 0 ? (
        <EmptyState>Ta bibliothèque est vide. Ajoute des fichiers .mp3 ou .wav dans le dossier music/.</EmptyState>
      ) : (
        <>
          <div className="toolbar">
            <div className="chips" role="tablist">
              <Chip active={view === "titres"} onClick={() => setView("titres")}>Titres</Chip>
              <Chip active={view === "artistes"} onClick={() => setView("artistes")}>Artistes</Chip>
            </div>
            {view === "titres" && (
              <div className="chips">
                <span className="chip-label">Trier par</span>
                {SORTS.map((s) => (
                  <Chip key={s.value} active={sort === s.value} onClick={() => setSort(s.value)}>
                    {s.label}
                  </Chip>
                ))}
              </div>
            )}
          </div>
          {view === "titres" ? (
            <TrackTable tracks={sorted} header />
          ) : (
            <div className="card-grid">
              {artists.map((a) => (
                <ArtistCard key={a.name} artist={a} />
              ))}
            </div>
          )}
        </>
      )}
    </>
  );
}

function Chip({ active, onClick, children }: { active: boolean; onClick: () => void; children: string }) {
  return (
    <button className={`chip ${active ? "active" : ""}`} aria-pressed={active} onClick={onClick}>
      {children}
    </button>
  );
}
