package org.adventure.simulation;

import org.adventure.society.Clan;
import org.adventure.society.ClanType;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.adventure.structure.OwnerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StructureLifecycleManager.
 */
public class StructureLifecycleManagerTest {
    private StructureLifecycleManager manager;
    private List<Structure> structures;
    private List<Clan> clans;
    
    @BeforeEach
    public void setUp() {
        manager = new StructureLifecycleManager();
        structures = new ArrayList<>();
        clans = new ArrayList<>();
    }
    
    @Test
    public void testDisastersCauseStructureDamage() {
        // Create structure
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        double initialHealth = structure.getHealth();
        
        // Simulate many disaster checks (every 1000 ticks)
        for (long tick = 0; tick < 100000; tick += 1000) {
            manager.simulateTick(structures, clans, tick);
        }
        
        // At least one disaster should have occurred (statistically)
        // With 100 checks and 5% chance each, probability of at least one disaster is very high
        assertTrue(structure.getHealth() < initialHealth || structure.isDestroyed(),
            "Structure should have taken disaster damage over 100 checks");
    }
    
    @Test
    public void testNeglectedStructuresDecay() {
        // Create clan with low treasury
        Clan poorClan = new Clan.Builder()
            .id("clan1")
            .name("Poor Clan")
            .type(ClanType.CLAN)
            .treasury(50.0) // Below maintenance threshold
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .lastActiveTick(0)
            .build();
        clans.add(poorClan);
        
        // Create structure owned by poor clan
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        // Simulate several neglect checks (every 7000 ticks)
        for (long tick = 0; tick < 35000; tick += 7000) {
            manager.simulateTick(structures, clans, tick);
        }
        
        // Structure should have decayed due to neglect
        assertTrue(structure.getHealth() < 100.0,
            "Neglected structure should decay over time");
    }
    
    @Test
    public void testDestroyedStructuresConvertToRuins() {
        // Create structure with very low health
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(1.0) // Almost destroyed
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        // Deal fatal damage
        structure.takeDamage(1.0, 0);
        
        // Simulate tick to trigger conversion
        manager.simulateTick(structures, clans, 1000);
        
        // Structure should be converted to ruin
        assertEquals(1, structures.size(), "Should still have 1 structure (now a ruin)");
        Structure ruin = structures.get(0);
        assertEquals(StructureType.ANCIENT_RUINS, ruin.getType(),
            "Destroyed structure should convert to ANCIENT_RUINS");
        assertTrue(ruin.getId().contains("_ruin"), "Ruin should have '_ruin' suffix");
        assertEquals(0.0, ruin.getHealth(), "Ruin should have 0 health");
    }
    
    @Test
    public void testAbandonedStructuresDecay() {
        // Create structure with no owner clan (abandoned)
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("nonexistent_clan")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        double initialHealth = structure.getHealth();
        
        // Simulate several neglect checks
        for (long tick = 0; tick < 35000; tick += 7000) {
            manager.simulateTick(structures, clans, tick);
        }
        
        // Abandoned structure should decay
        assertTrue(structure.getHealth() < initialHealth,
            "Abandoned structure (no owner clan) should decay");
    }
    
    @Test
    public void testWealthyClanStructuresDoNotDecay() {
        // Create wealthy clan
        Clan wealthyClan = new Clan.Builder()
            .id("clan1")
            .name("Wealthy Clan")
            .type(ClanType.CLAN)
            .treasury(1000.0) // Well above maintenance threshold
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .lastActiveTick(0)
            .build();
        clans.add(wealthyClan);
        
        // Create structure owned by wealthy clan
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        // Update clan to be active
        wealthyClan = new Clan.Builder(wealthyClan)
            .lastActiveTick(35000)
            .build();
        clans.set(0, wealthyClan);
        
        // Simulate several neglect checks
        for (long tick = 0; tick < 35000; tick += 7000) {
            manager.simulateTick(structures, clans, tick);
        }
        
        // Structure should NOT decay (wealthy and active clan)
        assertEquals(100.0, structure.getHealth(),
            "Structure owned by wealthy, active clan should not decay");
    }
    
    @Test
    public void testInactiveClanStructuresDecay() {
        // Create clan that's been inactive for long time
        Clan inactiveClan = new Clan.Builder()
            .id("clan1")
            .name("Inactive Clan")
            .type(ClanType.CLAN)
            .treasury(500.0) // Sufficient treasury but inactive
            .centerX(50)
            .centerY(50)
            .foundingTick(0)
            .lastActiveTick(0) // Not updated in a long time
            .build();
        clans.add(inactiveClan);
        
        // Create structure
        Structure structure = new Structure.Builder()
            .id("struct1")
            .type(StructureType.HOUSE)
            .ownerId("clan1")
            .ownerType(OwnerType.CLAN)
            .locationTileId("50,50")
            .health(100.0)
            .maxHealth(100.0)
            .createdAtTick(0)
            .build();
        structures.add(structure);
        
        // Simulate at tick 56000 (multiple of 7000, 50000+ ticks of inactivity triggers decay)
        manager.simulateTick(structures, clans, 56000);
        
        // Structure should decay due to clan inactivity
        assertTrue(structure.getHealth() < 100.0,
            "Structure owned by inactive clan should decay");
    }
}
