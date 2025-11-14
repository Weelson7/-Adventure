/**
 * Population simulation NPCs with lifecycle, marriage, and economic roles.
 * 
 * <h2>Architecture: NPC vs NamedNPC</h2>
 * 
 * <p>This package contains <b>NamedNPC</b> - population/society NPCs that are members
 * of clans and participate in economic and social simulation. These are distinct from
 * {@link org.adventure.character.NPC} which handles combat AI and region-based spawning.
 * 
 * <table border="1">
 *   <caption>NPC System Comparison</caption>
 *   <tr>
 *     <th>Aspect</th>
 *     <th>NamedNPC (this package)</th>
 *     <th>character.NPC</th>
 *   </tr>
 *   <tr>
 *     <td>Purpose</td>
 *     <td>Population simulation, clans, economy</td>
 *     <td>Combat encounters, AI behavior</td>
 *   </tr>
 *   <tr>
 *     <td>Generation</td>
 *     <td>At worldgen (tick 0) for clans</td>
 *     <td>Spawned in regions dynamically</td>
 *   </tr>
 *   <tr>
 *     <td>Lifecycle</td>
 *     <td>Birth, aging, marriage, death</td>
 *     <td>Combat spawning/despawning</td>
 *   </tr>
 *   <tr>
 *     <td>Behavior</td>
 *     <td>Economic roles, reproduction</td>
 *     <td>AI combat, trading, questing</td>
 *   </tr>
 *   <tr>
 *     <td>Membership</td>
 *     <td>Belongs to clans</td>
 *     <td>Independent, biome-based</td>
 *   </tr>
 * </table>
 * 
 * <h3>Integration</h3>
 * <p>NamedNPC can optionally link to a Character instance via {@code characterId} field
 * for stat tracking and combat integration. This allows a clan member to participate in
 * combat encounters while maintaining their social/economic role.
 * 
 * <h3>Key Classes</h3>
 * <ul>
 *   <li>{@link org.adventure.npc.NamedNPC} - Population NPC data model</li>
 *   <li>{@link org.adventure.npc.NPCGenerator} - Deterministic NPC generation</li>
 *   <li>{@link org.adventure.npc.NPCLifecycleManager} - Aging, marriage, reproduction, death</li>
 *   <li>{@link org.adventure.npc.PlayerNPCInteraction} - Player marriage/reproduction</li>
 * </ul>
 * 
 * @see org.adventure.character.NPC for combat NPCs
 * @see org.adventure.society.Clan for clan management
 */
package org.adventure.npc;
