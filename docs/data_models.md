# Core Data Models (stubs)

This document contains starter data model stubs for core systems. These are intentionally minimal and intended to be refined into language-specific classes/interfaces.


WorldGrid
- id: string (UUID)
- width: int
- height: int
- altitudeLayers: int
- seed: int
- createdAt: timestamp
- schemaVersion: int
- chunkSize: int (e.g., 64)
- chunks: list of Chunk metadata (x,y -> file/offset)

Chunk (persistence unit)
- id: string (tile coordinate prefix or chunk key)
- tiles: compressed list of Tile objects
- lastModifiedTick: int
- checksum: string



Tile
- id: string (coordinate-based key like "x:y:z")
- x: int, y: int, z: int (altitude layer)
- elevation: float
- temperature: float
- moisture: float
- biome: string (enum)
- waterType: enum {none,lake,river,ocean}
- features: list of RegionalFeature IDs
- flags: bitmask (e.g., passable, riverSource, protected)
- lastUpdatedTick: int

Example persisted Tile (JSON):
{
	"id": "12:34:2",
	"x": 12,
	"y": 34,
	"z": 2,
	"elevation": 342.5,
	"temperature": -5.4,
	"moisture": 0.3,
	"biome": "tundra",
	"waterType": "none",
	"features": ["volcano-0001"],
	"flags": 0,
	"lastUpdatedTick": 1024,
	"schemaVersion": 1
}


Plate
- id: string
- ownerWorldId: string
- seed: int
- cells: list of tile coordinate ranges or generator rule (Voronoi cell)
- movementVector: {dx:float, dy:float}
- convergenceType: enum {divergent, convergent, transform}
- boundaryHash: string (for debug/validation)



RegionalFeature
- id: string
- type: enum {volcano, magic_zone, submerged_city, ruin}
- tileIds: list of tile keys (could be multi-tile)
- rarityWeight: float
- placedAtTick: int



Structure
- id: string
- ownerId: string (character/clan/society)
- type: string
- locationTileId: string
- permissions: map<Role,AccessLevel>
- createdAtTick: int
- schemaVersion: int



Character (stub)
- id: string
- name: string
- species/race: string
- stats: map<string,int>
- derivedStats: map<string,float> (e.g., maxMana)
- manaPool: {current:int, max:int} (optional, if magic enabled)
- castingStat: string (e.g., intelligence) — used for spell calculations
- traits: list of trait IDs
- inventory: list of item IDs
- ownerSocietyId: string (optional)
- lastOnlineTick: int

Example Character (JSON excerpt):
{
	"id": "char-0001",
	"name": "Alyn",
	"race": "human",
	"stats": {"strength":10, "intelligence":14},
	"derivedStats": {"maxMana": 30},
	"manaPool": {"current":30, "max":30},
	"castingStat": "intelligence",
	"traits": ["fast_learner"],
	"inventory": [],
	"schemaVersion": 1
}


Item
- id: string
- prototypeId: string
- ownerId: string
- durability: float
- properties: map<string, any> (e.g., enchantments)
- historyReferenceId: string (optional, ties to story)
- createdAtTick: int
- schemaVersion: int



Society / Clan / Kingdom (stub)
- id: string
- name: string
- type: enum {clan, kingdom, guild}
- members: list of character IDs
- treasury: numeric
- relationships: map<otherSocietyId, RelationshipRecord>
- foundingTick: int
- lastActiveTick: int
- schemaVersion: int

RelationshipRecord
- targetSocietyId: string
- reputation: float (e.g., -100 to +100)
- influence: float
- alignment: float
- raceAffinity: float
- lastUpdatedTick: int

Region
- id: string (e.g., "region_x100_y200")
- worldId: string
- bounds: {minX, minY, maxX, maxY, minZ, maxZ}
- isActive: bool (true if players present)
- lastProcessedTick: int
- activeTickRateMultiplier: float (default 1)
- backgroundTickRateMultiplier: float (default 1/60)
- npcIds: list of character IDs
- structureIds: list of structure IDs
- eventQueue: list of pending event IDs
- schemaVersion: int

Example Region (JSON excerpt):
{
  "id": "region_x0_y0",
  "worldId": "world-001",
  "bounds": {"minX":0, "minY":0, "maxX":64, "maxY":64, "minZ":0, "maxZ":4},
  "isActive": true,
  "lastProcessedTick": 5000,
  "activeTickRateMultiplier": 1.0,
  "backgroundTickRateMultiplier": 0.016667,
  "npcIds": ["npc-001", "npc-002"],
  "structureIds": ["struct-100"],
  "eventQueue": [],
  "schemaVersion": 1
}

Story
- id: string
- title: string
- originTileId: string
- originTick: int
- baseProbability: float
- hopCount: int
- maxHops: int
- saturationScore: float
- linkedObjectIds: list of item/structure IDs
- linkedCharacterIds: list of character IDs
- status: enum {active, archived, resolved}
- lastProcessedTick: int
- schemaVersion: int

Event
- id: string
- type: string (e.g., "volcanic_eruption", "diplomatic_crisis")
- targetRegionId: string
- triggeredAtTick: int
- expiresAtTick: int (optional)
- priority: int
- effectData: map<string, any>
- schemaVersion: int

CraftingRecipe
- id: string
- name: string
- category: string
- requiredMaterials: list of {itemPrototypeId, quantity}
- requiredTools: list of tool prototype IDs
- requiredProficiency: int (min proficiency to craft)
- outputItemPrototypeId: string
- outputQuantity: int
- craftingTime: int (ticks)
- failureChance: float
- schemaVersion: int

Notes & guidance
- Use compact, chunked storage for the WorldGrid (e.g., region chunks) to avoid keeping all tiles in-memory.
- Persist only deltas for highly dynamic state where possible; base terrain and deterministic features can be regenerated from seed + parameter set.
- Each persisted model MUST include `schemaVersion` and `type` fields. Prefer migration scripts for upgrades; support streaming region-by-region migration for very large worlds.
- Use CRC/checksums for persisted chunks and keep the last N backups for recovery.
- Keep dynamic runtime-only fields (e.g., transient caches) out of persisted schemas; store minimal state needed for resync.

