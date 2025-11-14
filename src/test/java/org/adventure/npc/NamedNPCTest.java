package org.adventure.npc;

import org.adventure.society.Clan;
import org.adventure.structure.Structure;
import org.adventure.structure.StructureType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NamedNPC data model.
 */
public class NamedNPCTest {
    
    @Test
    public void testBuilder() {
        NamedNPC npc = new NamedNPC.Builder()
            .id("npc-1")
            .name("Aldric")
            .clanId("clan-1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.FARMER)
            .homeStructureId("house-1")
            .fertility(90)
            .birthTick(0L)
            .build();
        
        assertEquals("npc-1", npc.getId());
        assertEquals("Aldric", npc.getName());
        assertEquals(25, npc.getAge());
        assertEquals(Gender.MALE, npc.getGender());
        assertEquals(NPCJob.FARMER, npc.getJob());
        assertFalse(npc.isMarried());
        assertFalse(npc.isChild());
        assertTrue(npc.isReproductiveAge());
    }
    
    @Test
    public void testMarriage() {
        NamedNPC male = new NamedNPC.Builder()
            .id("npc-1")
            .name("Aldric")
            .clanId("clan-1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.FARMER)
            .homeStructureId("house-1")
            .fertility(90)
            .birthTick(0L)
            .build();
        
        NamedNPC female = new NamedNPC.Builder()
            .id("npc-2")
            .name("Aria")
            .clanId("clan-1")
            .age(23)
            .gender(Gender.FEMALE)
            .job(NPCJob.UNEMPLOYED)
            .homeStructureId("house-1")
            .fertility(95)
            .birthTick(0L)
            .build();
        
        assertFalse(male.isMarried());
        assertFalse(female.isMarried());
        
        male.setSpouseId(female.getId());
        female.setSpouseId(male.getId());
        
        assertTrue(male.isMarried());
        assertTrue(female.isMarried());
        assertEquals(female.getId(), male.getSpouseId());
        assertEquals(male.getId(), female.getSpouseId());
    }
    
    @Test
    public void testChildren() {
        NamedNPC parent = new NamedNPC.Builder()
            .id("npc-1")
            .name("Aldric")
            .clanId("clan-1")
            .age(30)
            .gender(Gender.MALE)
            .job(NPCJob.FARMER)
            .homeStructureId("house-1")
            .fertility(85)
            .birthTick(0L)
            .build();
        
        assertTrue(parent.getChildrenIds().isEmpty());
        
        parent.addChild("child-1");
        parent.addChild("child-2");
        
        assertEquals(2, parent.getChildrenIds().size());
        assertTrue(parent.getChildrenIds().contains("child-1"));
        assertTrue(parent.getChildrenIds().contains("child-2"));
    }
    
    @Test
    public void testReproductiveAge() {
        NamedNPC child = createNPC(10, Gender.MALE);
        assertFalse(child.isReproductiveAge());
        assertTrue(child.isChild());
        
        NamedNPC youngAdult = createNPC(20, Gender.FEMALE);
        assertTrue(youngAdult.isReproductiveAge());
        assertFalse(youngAdult.isChild());
        
        NamedNPC middleAged = createNPC(40, Gender.MALE);
        assertTrue(middleAged.isReproductiveAge());
        
        NamedNPC elder = createNPC(60, Gender.FEMALE);
        assertFalse(elder.isReproductiveAge());
    }
    
    @Test
    public void testPlayerNPC() {
        NamedNPC player = new NamedNPC.Builder()
            .id("player-1")
            .name("PlayerName")
            .clanId("clan-1")
            .age(25)
            .gender(Gender.MALE)
            .job(NPCJob.WARRIOR)
            .homeStructureId("house-1")
            .fertility(90)
            .birthTick(0L)
            .isPlayer(true)
            .build();
        
        assertTrue(player.isPlayer());
        
        NamedNPC npc = createNPC(25, Gender.FEMALE);
        assertFalse(npc.isPlayer());
    }
    
    private NamedNPC createNPC(int age, Gender gender) {
        return new NamedNPC.Builder()
            .id(UUID.randomUUID().toString())
            .name("TestNPC")
            .clanId("clan-1")
            .age(age)
            .gender(gender)
            .job(NPCJob.UNEMPLOYED)
            .homeStructureId("house-1")
            .fertility(50)
            .birthTick(0L)
            .build();
    }
}
