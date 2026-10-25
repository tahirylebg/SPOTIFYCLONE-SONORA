import { NavLink } from "react-router-dom";
import { useLibrary } from "../library/LibraryProvider";
import { usePlayer } from "../player/PlayerProvider";
import { Cover } from "./Cover";
import { Icon, type IconName } from "./Icon";

export const NAV_ITEMS: { to: string; label: string; icon: IconName }[] = [
  { to: "/", label: "Accueil", icon: "home" },
  { to: "/recherche", label: "Rechercher", icon: "search" },
  { to: "/bibliotheque", label: "Bibliothèque", icon: "library" },
  { to: "/decouvertes", label: "Découvertes", icon: "explore" },
];

export function Sidebar() {
  const { recent } = useLibrary();
  const { play, current } = usePlayer();
  const recents = recent.slice(0, 8);

  return (
    <aside className="sidebar">
      <div className="brand">
        <Icon name="music" size={24} className="brand-logo" />
        <span className="brand-name">Sonora</span>
      </div>

      <nav className="nav">
        {NAV_ITEMS.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.to === "/"} className="nav-item">
            <Icon name={item.icon} size={22} />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="nav-divider" />
      <div className="section-title">Écoutés récemment</div>
      <div className="recents">
        {recents.length === 0 ? (
          <p className="recent-empty">Tes écoutes apparaîtront ici.</p>
        ) : (
          recents.map((track) => (
            <button
              key={track.id}
              className={`recent-item ${current?.id === track.id ? "is-playing" : ""}`}
              onDoubleClick={() => play(track, recents)}
              title="Double-clique pour écouter"
            >
              <Cover url={track.coverUrl} label={track.title} size={40} />
              <span className="recent-text">
                <span className="recent-title">{track.title}</span>
                <span className="recent-artist">{track.artist}</span>
              </span>
            </button>
          ))
        )}
      </div>
    </aside>
  );
}

/** Barre de navigation en bas de l'écran sur mobile (remplace la sidebar). */
export function MobileNav() {
  return (
    <nav className="mobile-nav">
      {NAV_ITEMS.map((item) => (
        <NavLink key={item.to} to={item.to} end={item.to === "/"} className="mobile-nav-item">
          <Icon name={item.icon} size={22} />
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
}
