import React, { useState, useMemo } from 'react';
import './CyberTarotDeck.css';

/**
 * 78 CYBER TAROT DECK - DIGITAL CIRCUITRY EDITION
 * React component utilizing CSS Grid, Neon Glow effects, and interactive SVG circuitry.
 */

// 1. Data Definitions for 78 Cards (22 Major Arcana + 56 Minor Arcana)
const MAJOR_ARCANA = [
  { index: 0, name: "The Glitch", traditional: "The Fool", suit: "Major", type: "major", rarity: "Common", circuitry: "M10 50 Q 50 20, 90 50 T 170 50", desc: "A clean terminal screen suddenly displaying erratic white-noise visual code. A digital wanderer stepping blindly into an uncompiled codebase." },
  { index: 1, name: "The Netrunner", traditional: "The Magician", suit: "Major", type: "major", rarity: "Rare", circuitry: "M30 20 H 170 V 80 H 30 Z", desc: "An elite operator with dual-monitors, neural interfaces pulsing, typing cybernetic scripts to manifest reality directly onto the server grid." },
  { index: 2, name: "The AI Oracle", traditional: "The High Priestess", suit: "Major", type: "major", rarity: "Epic", circuitry: "M10 80 C 40 10, 60 10, 90 80 S 140 150, 170 80", desc: "A massive blue neural hologram sitting behind a cascade of glowing terminal logs, embodying the infinite knowledge of the offline network." },
  { index: 3, name: "The Mainframe", traditional: "The Empress", suit: "Major", type: "major", rarity: "Epic", circuitry: "M10 10 L 90 90 L 170 10 M 90 90 V 170", desc: "A state-of-the-art climate-controlled server room where automated mechanical arms safely install endless NVMe storage and silicon chips." },
  { index: 4, name: "The SysAdmin", traditional: "The Emperor", suit: "Major", type: "major", rarity: "Rare", circuitry: "M10 10 H 170 V 170 H 10 Z M 50 50 H 130 V 130 H 50 Z", desc: "A sovereign terminal seat overlooking a giant glowing wall of global threat monitoring maps. Strong security firewalls pulsing blue." },
  { index: 5, name: "The Encryption Key", traditional: "The Hierophant", suit: "Major", type: "major", rarity: "Rare", circuitry: "M50 20 L 150 20 L 100 90 L 150 160 L 50 160 Z", desc: "An ancient consensus vault containing the root TLS certificate authorities, shining with pristine algorithmic security layers." },
  { index: 6, name: "The Dual Link", traditional: "The Lovers", suit: "Major", type: "major", rarity: "Common", circuitry: "M10 90 Q 90 10, 170 90 T 90 170 Z", desc: "Two cyborg avatars in cyberspace sharing an optical fiber cable, syncing their neural networks with perfect high-speed handshakes." },
  { index: 7, name: "The Hyperloop", traditional: "The Chariot", suit: "Major", type: "major", rarity: "Rare", circuitry: "M10 50 H 170 M 50 10 L 90 50 L 50 90 M 130 10 L 90 50 L 130 90", desc: "A sleek neon-blue capsule flying down a digital vacuum tube, guided by magnetic rails and extreme computational acceleration." },
  { index: 8, name: "Overclock", traditional: "Strength", suit: "Major", type: "major", rarity: "Common", circuitry: "M10 90 C 30 30, 150 150, 170 90", desc: "A massive liquid nitrogen cooling block handling a fiery-red processor that is pushed to 8.0GHz, tamed by software algorithms." },
  { index: 9, name: "The Offline Dev", traditional: "The Hermit", suit: "Major", type: "major", rarity: "Common", circuitry: "M90 10 V 170 M 10 90 H 170", desc: "A developer sitting in a completely dark, air-gapped server room, lit only by green phosphor lines of terminal code." },
  { index: 10, name: "The RNG Lootbox", traditional: "Wheel of Fortune", suit: "Major", type: "major", rarity: "Rare", circuitry: "M90 90 A 80 80 0 1 0 90 91 Z", desc: "A cosmic digital slot machine displaying randomized blockchain hashes, glowing with neon green and purple indicators." },
  { index: 11, name: "Consensus Protocol", traditional: "Justice", suit: "Major", type: "major", rarity: "Common", circuitry: "M30 30 L 150 150 M 150 30 L 30 150", desc: "A smart-contract arbitration terminal displaying a perfectly balanced scale made of green laser light, resolving a file hash conflict." },
  { index: 12, name: "The Sandbox Mode", traditional: "The Hanged Man", suit: "Major", type: "major", rarity: "Common", circuitry: "M90 10 L 10 90 L 90 170 L 170 90 Z", desc: "A virtual machine container suspended in mid-air inside a dark glowing grid, with execution paused at a critical debug breakpoint." },
  { index: 13, name: "System Reboot", traditional: "Death", suit: "Major", type: "major", rarity: "Rare", circuitry: "M10 10 L 170 170 M 170 10 L 10 170 M 10 90 H 170 M 90 10 V 170", desc: "A terminal showing a clean command terminal executing 'shutdown -r now'. Relays clicking open, and dark circuits turning back on." },
  { index: 14, name: "Load Balancer", traditional: "Temperance", suit: "Major", type: "major", rarity: "Common", circuitry: "M10 90 Q 50 10, 90 90 T 170 90", desc: "An intricate router distributing incoming traffic arrows smoothly across multiple servers, keeping core temperatures cool and balanced." },
  { index: 15, name: "The Trojan Horse", traditional: "The Devil", suit: "Major", type: "major", rarity: "Epic", circuitry: "M30 170 L 90 10 L 150 170 Z M 60 110 H 120", desc: "A glowing neon red icon representing a seemingly harmless update file, but hiding dark circuitry vines wrapping around CPU cores." },
  { index: 16, name: "The Mainframe Crash", traditional: "The Tower", suit: "Major", type: "major", rarity: "Legend", circuitry: "M10 10 L 170 90 L 10 170 M 90 10 V 170", desc: "A giant mainframe tower exploding in neon-orange sparks as lightning bolts of sudden voltage surges strike through fried circuitry panels." },
  { index: 17, name: "The Fiber Optic Star", traditional: "The Star", suit: "Major", type: "major", rarity: "Rare", circuitry: "M90 10 L 110 70 L 170 90 L 110 110 L 90 170 L 70 110 L 10 90 L 70 70 Z", desc: "A single laser beam traveling over an ultra-clear fiber optic line, casting a brilliant starlight aura over the dark web architecture." },
  { index: 18, name: "The Dark Web", traditional: "The Moon", suit: "Major", type: "major", rarity: "Epic", circuitry: "M10 90 A 80 80 0 0 1 170 90", desc: "A glowing, crescent-shaped digital gateway with deep purple rays, reflecting off a lake of dark encrypted data streams." },
  { index: 19, name: "The Solar Array", traditional: "The Sun", suit: "Major", type: "major", rarity: "Legend", circuitry: "M90 90 A 40 40 0 1 0 90 91 Z M 90 10 V 40 M 90 140 V 170 M 10 90 H 40 M 140 90 H 170", desc: "A giant glowing orange fusion core grid, pumping clean, infinite gigawatts of energy directly into a thriving neon megacity." },
  { index: 20, name: "The Code Review", traditional: "Judgement", suit: "Major", type: "major", rarity: "Rare", circuitry: "M10 10 H 170 M 10 170 H 170 M 50 10 V 170 M 130 10 V 170", desc: "A massive glowing green 'Approved' terminal prompt, with thousands of developer nodes singing in consensus as the PR merges." },
  { index: 21, name: "The Global Network", traditional: "The World", suit: "Major", type: "major", rarity: "Legend", circuitry: "M90 90 A 60 60 0 1 0 90 91 Z M 10 90 H 170 M 90 10 V 170", desc: "A glowing holographic globe encased in a dense, shimmering mesh of high-speed satellite signals and fiber-optic cables." },
];

const MINOR_SUITS = [
  { name: "Cores", icon: "🔥", desc: "Computational output, execution threads, and energetic fire.", keyElement: "Wands" },
  { name: "RAM", icon: "💧", desc: "Volatile buffers, state management registers, and streaming memory.", keyElement: "Cups" },
  { name: "Terminals", icon: "🗡️", desc: "Input commands, security vectors, and digital execution paths.", keyElement: "Swords" },
  { name: "Nodes", icon: "🪙", desc: "Hardware chips, ledger transactions, and material crypto-tokens.", keyElement: "Pentacles" },
];

// Generate 56 Minor Arcana dynamically with appropriate Cyberpunk nomenclature
const generateMinorArcana = () => {
  const cards = [];
  let index = 22;

  MINOR_SUITS.forEach(suitInfo => {
    for (let value = 1; value <= 14; value++) {
      let cardName = "";
      let traditionalName = "";
      let rarity = "Common";
      let circuitry = "";

      // Determine traditional and cybernetic rank names
      if (value === 1) {
        cardName = `Ace of ${suitInfo.name}`;
        traditionalName = `Ace of ${suitInfo.keyElement}`;
        rarity = "Rare";
        circuitry = "M50 50 H 130 V 130 H 50 Z";
      } else if (value <= 10) {
        cardName = `${value} of ${suitInfo.name}`;
        traditionalName = `${value} of ${suitInfo.keyElement}`;
        rarity = value >= 8 ? "Rare" : "Common";
        circuitry = `M10 50 H ${10 + value * 10} V 130 H 10 Z`;
      } else if (value === 11) {
        cardName = `Apprentice of ${suitInfo.name}`;
        traditionalName = `Page of ${suitInfo.keyElement}`;
        rarity = "Rare";
        circuitry = "M20 20 L 160 160 M 160 20 L 20 160";
      } else if (value === 12) {
        cardName = `Courier of ${suitInfo.name}`;
        traditionalName = `Knight of ${suitInfo.keyElement}`;
        rarity = "Rare";
        circuitry = "M10 90 H 170 L 130 50 M 10 90 L 50 130";
      } else if (value === 13) {
        cardName = `Processor of ${suitInfo.name}`;
        traditionalName = `Queen of ${suitInfo.keyElement}`;
        rarity = "Epic";
        circuitry = "M10 90 C 50 10, 130 10, 170 90 M 30 50 H 150";
      } else {
        cardName = `Admin of ${suitInfo.name}`;
        traditionalName = `King of ${suitInfo.keyElement}`;
        rarity = "Legend";
        circuitry = "M10 10 H 170 V 170 H 10 Z M 30 30 H 150 V 150 H 30 Z M 50 50 H 130 V 130 H 50 Z";
      }

      // Generate distinct lore descriptions per suit/value
      let desc = "";
      if (suitInfo.name === "Cores") {
        desc = `Pumping high-intensity energy across custom heatpipes. Represents a ${traditionalName} focusing on processing speed, power expansion, and digital drive.`;
      } else if (suitInfo.name === "RAM") {
        desc = `A rapid flash dynamic bank syncing cache entries in nanoseconds. Represents a ${traditionalName} centering data buffers, memory, emotional flow, and temporary variables.`;
      } else if (suitInfo.name === "Terminals") {
        desc = `Laser line terminal interface parsing active security parameters. Represents a ${traditionalName} outlining command scripts, firewalls, cognitive conflicts, and software decisions.`;
      } else {
        desc = `A pristine gold-plated semiconductor wafer storing local ledger keys. Represents a ${traditionalName} signifying physical hardware, silicon nodes, database assets, and tangible tokens.`;
      }

      cards.push({
        index,
        name: cardName,
        traditional: traditionalName,
        suit: suitInfo.name,
        type: "minor",
        rarity,
        circuitry,
        desc
      });
      index++;
    }
  });

  return cards;
};

const ALL_CARDS = [...MAJOR_ARCANA, ...generateMinorArcana()];

export default function CyberTarotDeck() {
  const [search, setSearch] = useState("");
  const [selectedSuit, setSelectedSuit] = useState("All");
  const [selectedRarity, setSelectedRarity] = useState("All");
  const [inspectCard, setInspectCard] = useState(null);
  const [flippedCards, setFlippedCards] = useState({});

  // Filter cards based on search inputs, suit, and rarity
  const filteredCards = useMemo(() => {
    return ALL_CARDS.filter(card => {
      const matchesSearch = card.name.toLowerCase().includes(search.toLowerCase()) || 
                            card.traditional.toLowerCase().includes(search.toLowerCase()) ||
                            card.desc.toLowerCase().includes(search.toLowerCase());
      const matchesSuit = selectedSuit === "All" || card.suit === selectedSuit;
      const matchesRarity = selectedRarity === "All" || card.rarity === selectedRarity;
      return matchesSearch && matchesSuit && matchesRarity;
    });
  }, [search, selectedSuit, selectedRarity]);

  const toggleFlip = (cardIndex) => {
    setFlippedCards(prev => ({
      ...prev,
      [cardIndex]: !prev[cardIndex]
    }));
  };

  const getRarityClass = (rarity) => {
    switch (rarity) {
      case 'Legend': return 'rarity-legend';
      case 'Epic': return 'rarity-epic';
      case 'Rare': return 'rarity-rare';
      default: return 'rarity-common';
    }
  };

  return (
    <div className="cyber-deck-container">
      {/* Header and Telemetry */}
      <header className="cyber-deck-header">
        <h1 className="neon-text-magenta">CYBERNETIC TAROT ARCHIVE</h1>
        <p className="subtitle">78-NODE CRYPTOGRAPHIC SPECIFICATION REPOSITORY</p>
        <div className="header-stats">
          <div className="stat-node">SYSTEM: <span>ONLINE</span></div>
          <div className="stat-node">NODES: <span>78/78 LOADED</span></div>
          <div className="stat-node">MATCHED: <span>{filteredCards.length} NODES</span></div>
        </div>
      </header>

      {/* Control Console Grid */}
      <section className="control-console">
        <div className="input-field">
          <label>SEARCH ENCRYPTED CODENAME</label>
          <input 
            type="text" 
            placeholder="Search e.g. 'Glitch' or 'Cores'..." 
            value={search} 
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <label>FILTER BY PROTOCOL SUIT</label>
          <div className="button-grid">
            {['All', 'Major', 'Cores', 'RAM', 'Terminals', 'Nodes'].map(suit => (
              <button 
                key={suit}
                className={selectedSuit === suit ? 'active' : ''}
                onClick={() => setSelectedSuit(suit)}
              >
                {suit === 'Major' ? 'Major Arcana' : suit}
              </button>
            ))}
          </div>
        </div>

        <div className="filter-group">
          <label>FILTER BY SYSTEM RARITY</label>
          <div className="button-grid">
            {['All', 'Common', 'Rare', 'Epic', 'Legend'].map(rarity => (
              <button 
                key={rarity}
                className={`${selectedRarity === rarity ? 'active' : ''} button-${rarity.toLowerCase()}`}
                onClick={() => setSelectedRarity(rarity)}
              >
                {rarity}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* CSS Grid of Cyber Cards */}
      <main className="cards-grid">
        {filteredCards.map(card => {
          const isFlipped = flippedCards[card.index] || false;
          return (
            <div 
              key={card.index} 
              className={`cyber-card-wrapper ${getRarityClass(card.rarity)}`}
              onClick={() => toggleFlip(card.index)}
            >
              <div className={`cyber-card-inner ${isFlipped ? 'is-flipped' : ''}`}>
                
                {/* CARD BACK (Circuit board state) */}
                <div className="cyber-card-back">
                  <div className="circuit-pattern-overlay"></div>
                  <div className="card-back-core">
                    <div className="cyber-chip"></div>
                    <div className="neon-circle"></div>
                    <span className="node-num">N-{card.index}</span>
                  </div>
                  <div className="status-panel">
                    <div className="pulse-dot"></div>
                    <span className="firmware-tag">FW_V4.8.2</span>
                  </div>
                </div>

                {/* CARD FRONT (Decrypted core) */}
                <div className="cyber-card-front">
                  <div className="card-header">
                    <span className="node-id">NODE #{String(card.index).padStart(2, '0')}</span>
                    <span className={`rarity-pill ${card.rarity.toLowerCase()}`}>{card.rarity}</span>
                  </div>

                  {/* SVG Circuit Art Block */}
                  <div className="card-circuit-art">
                    <svg viewBox="0 0 180 180" width="100%" height="100%">
                      <defs>
                        <linearGradient id={`grad-${card.index}`} x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" stopColor="var(--glow-color)" stopOpacity="1" />
                          <stop offset="100%" stopColor="#0a0a16" stopOpacity="0.8" />
                        </linearGradient>
                      </defs>
                      {/* Silicon Grid Backing */}
                      <path d="M10 10 H170 V170 H10 Z" fill="none" stroke="#ffffff" strokeOpacity="0.04" strokeWidth="2" />
                      <line x1="90" y1="0" x2="90" y2="180" stroke="#ffffff" strokeOpacity="0.04" />
                      <line x1="0" y1="90" x2="180" y2="90" stroke="#ffffff" strokeOpacity="0.04" />
                      {/* Active Trace */}
                      <path 
                        d={card.circuitry || "M30 50 H150 V130 H30 Z"} 
                        fill="none" 
                        stroke={`url(#grad-${card.index})`} 
                        strokeWidth="3.5" 
                        strokeLinecap="round"
                        className="animated-circuit"
                      />
                      {/* Terminal node indicators */}
                      <circle cx="90" cy="90" r="6" fill="var(--glow-color)" className="glowing-node" />
                      <circle cx="10" cy="90" r="3" fill="var(--glow-color)" />
                      <circle cx="170" cy="90" r="3" fill="var(--glow-color)" />
                    </svg>
                  </div>

                  <div className="card-body">
                    <h3 className="card-name">{card.name}</h3>
                    <h4 className="card-traditional">[{card.traditional}]</h4>
                    <p className="card-description">{card.desc}</p>
                  </div>

                  <div className="card-footer">
                    <button 
                      className="inspect-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        setInspectCard(card);
                      }}
                    >
                      DIAGNOSE MODULE
                    </button>
                  </div>
                </div>

              </div>
            </div>
          );
        })}

        {filteredCards.length === 0 && (
          <div className="empty-state">
            <p className="neon-text-red">⚠️ SIGNAL LOST: NO HARDWARE NODES MATCH SEARCH DEFINITIONS</p>
            <button className="reset-search-btn" onClick={() => { setSearch(""); setSelectedSuit("All"); setSelectedRarity("All"); }}>
              RESTORE TELEMETRY BACKUP
            </button>
          </div>
        )}
      </main>

      {/* Inspected Card Detail Dialog */}
      {inspectCard && (
        <div className="inspect-overlay" onClick={() => setInspectCard(null)}>
          <div className="inspect-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="dialog-header">
              <h2>DIAGNOSING MODULE: {inspectCard.name.toUpperCase()}</h2>
              <button className="close-dialog" onClick={() => setInspectCard(null)}>×</button>
            </div>
            <div className="dialog-body">
              <div className="dialog-card-preview">
                <svg viewBox="0 0 180 180" className="preview-svg">
                  <path d={inspectCard.circuitry} fill="none" stroke="var(--glow-color)" strokeWidth="4" />
                  <circle cx="90" cy="90" r="8" fill="var(--glow-color)" />
                </svg>
              </div>
              <div className="dialog-specs">
                <div className="spec-row">
                  <span className="label">TRADITIONAL INTERACTION</span>
                  <span className="val">{inspectCard.traditional}</span>
                </div>
                <div className="spec-row">
                  <span className="label">SUIT MODULE TYPE</span>
                  <span className="val">{inspectCard.suit}</span>
                </div>
                <div className="spec-row">
                  <span className="label">THREAT RARITY TIER</span>
                  <span className="val text-glowing">{inspectCard.rarity}</span>
                </div>
                <div className="spec-desc">
                  <h3>CORE SPECIFICATIONS LOGS</h3>
                  <p>{inspectCard.desc}</p>
                </div>
                <div className="firmware-flash">
                  <div className="flash-ready">READY FOR FIRMWARE DEPLOYMENT</div>
                  <button className="flash-btn" onClick={() => alert(`Flashed Node #${inspectCard.index} successfully to TPM module.`)}>
                    FLASH KEY ENCRYPT MODULE
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
