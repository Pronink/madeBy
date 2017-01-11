/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MadeBy;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Pronink
 */
public class Main extends JavaPlugin implements Listener
{

    private final int CRAFTEADO = 0;
    private final int ENCANTADO = 1;
    private final int FUSIONADO = 2;

    @Override
    public void onEnable()
    {
	getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    // AL CRAFTEAR
    @EventHandler
    public void craftItem(PrepareItemCraftEvent event)
    {
	boolean isRepair = false;
	// SI ES UNA REPARACION
	Inventory inventory = event.getInventory();
	if (inventory instanceof CraftingInventory)
	{
	    CraftingInventory craftingTable = (CraftingInventory) inventory;
	    ItemStack is[] = craftingTable.getContents();

	    ItemStack firstItem = null;
	    ItemStack secondItem = null;
	    for (int i = 1; i < is.length; i++) // Recorro la mesa de crafteo / la mesa del jugador. Empiezo en 1 ya que el 0 es el resultado
	    {
		if (is[i].getAmount() == 1 && is[i].getMaxStackSize() == 1) // Si es 1 item y no se estaquea. Aqui compruebo si es reparacion
		{
		    if (firstItem == null)
		    {
			firstItem = is[i];
		    }
		    else if (secondItem == null)
		    {
			secondItem = is[i];
			isRepair = true; // Por ahora puede ser reparacion si no encuentra otro item (el tercero)
		    }
		    else
		    {
			isRepair = false;
		    }
		}
	    }
	    if (isRepair && firstItem.getData().getItemType().equals(secondItem.getData().getItemType())) // SI ES REPARACION Y SON IGUALES
	    {
		String player = event.getView().getPlayer().getName();
		// Primero tengo que combinar y luego añadir lo nuevo. Tanto aqui como en el anvil
		List<String> firstMeta = firstItem.getItemMeta().getLore();
		List<String> secondMeta = secondItem.getItemMeta().getLore();
		List<String> loreList = combineLores(firstMeta, secondMeta);  // Combino

		loreList = addNewPlayerTo(loreList, FUSIONADO, player);       // Agrego
		ItemStack item = event.getRecipe().getResult();
		ItemMeta meta = item.getItemMeta();
		meta.setLore(loreList);
		item.setItemMeta(meta);
		event.getInventory().setResult(item);
	    }
	    else // NO ES REPARACION, SOLO ES CRAFTEO
	    {
		ItemStack item = event.getRecipe().getResult();
		if (item.getMaxStackSize() == 1)
		{
		    ItemMeta meta = item.getItemMeta();
		    List<String> loreList = verifyLoreList(meta.getLore());

		    String player = event.getView().getPlayer().getName();
		    loreList = addNewPlayerTo(loreList, CRAFTEADO, player);

		    meta.setLore(loreList);
		    item.setItemMeta(meta);
		    event.getInventory().setResult(item);
		}
	    }
	}
    }

    // AL ENCANTAR
    @EventHandler//(priority = EventPriority.HIGHEST)
    public void onEnchantItem(EnchantItemEvent event)
    {
	String player = event.getEnchanter().getName();

	ItemStack item = event.getItem();
	ItemMeta meta = item.getItemMeta();
	List<String> loreList = meta.getLore();

	loreList = addNewPlayerTo(loreList, ENCANTADO, player);

	meta.setLore(loreList);
	item.setItemMeta(meta);
    }

    // AL USAR EL YUNQUE
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
	if (event.getInventory() instanceof AnvilInventory)
	{
	    String player = event.getWhoClicked().getName();
	    AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
	    ItemStack firstItem = anvilInventory.getItem(0);
	    ItemStack secondItem = anvilInventory.getItem(1);
	    if (firstItem != null && secondItem != null)
	    {
		List<String> firstLores = firstItem.getItemMeta().getLore();
		List<String> secondLores = secondItem.getItemMeta().getLore();

		ItemStack resultItem = anvilInventory.getItem(2);
		if (resultItem != null) // AQUI SOLO ENTRA EN LA SEGUNDA VEZ. Es un bug menor
		{
		    List<String> resultLores = combineLores(firstLores, secondLores);
		    resultLores = addNewPlayerTo(resultLores, FUSIONADO, player);
		    ItemMeta meta = resultItem.getItemMeta();
		    meta.setLore(resultLores);
		    resultItem.setItemMeta(meta);
		    anvilInventory.setItem(2, resultItem);
		}
	    }
	}
    }

    private List<String> verifyLoreList(List<String> loreList) // Creo el boceto inicial
    {
	if (loreList == null || loreList.size() != 3) // Con esto me aseguro que si es corrupto lo borro
	{
	    loreList = new ArrayList<>();
	    for (int i = 0; i < 3; i++)
	    {
		loreList.add("");
	    }
	}
	return loreList;
    }

    private List<String> addNewPlayerTo(List<String> loreList, int loreAtt, String player)
    {
	if (loreList == null) // Me aseguro antes de que exista
	{
	    loreList = verifyLoreList(loreList);
	}
	String line = loreList.get(loreAtt);
	if (line.equals(""))
	{
	    switch (loreAtt)
	    {
		case 0:
		    line = ChatColor.RED + "Crafteado por: " + ChatColor.WHITE + player;
		    break;
		case 1:
		    line = ChatColor.LIGHT_PURPLE + "Encantado por: " + ChatColor.WHITE + player;
		    break;
		case 2:
		    line = ChatColor.GRAY + "Fusionado por: " + ChatColor.WHITE + player;
		    break;
		default:
		    break;
	    }
	}
	else
	{
	    boolean addName = true;
	    StringTokenizer names = new StringTokenizer(loreList.get(loreAtt).substring(19));
	    while (names.hasMoreTokens())
	    {
		if (names.nextToken().equals(player))
		{
		    addName = false;
		}
	    }
	    if (addName)
	    {
		line += " " + player;
	    }
	}
	loreList.set(loreAtt, line);
	return loreList;
    }

    private List<String> combineLores(List<String> firstMeta, List<String> secondMeta)
    {
	// Verifico que el antiguo meta tenga algo, aunque sea vacio
	if (firstMeta == null)
	{
	    firstMeta = verifyLoreList(firstMeta);
	}
	if (secondMeta == null)
	{
	    secondMeta = verifyLoreList(secondMeta);
	}
	// Preparo el nuevo meta
	List<String> newMeta = new ArrayList<>();
	newMeta.add("");
	newMeta.add("");
	newMeta.add("");
	for (int i = 0; i < 3; i++)
	{
	    if (firstMeta.get(i).equals("") && secondMeta.get(i).equals("")) // Si los dos estan vacios, no hay nada que conbinar // CREO QUE ESTO PUEDE RETORNAR NULL SI NADA SE LE PASA
	    {
		newMeta.set(i, "");
	    }
	    else if (!firstMeta.get(i).equals("") && !secondMeta.get(i).equals("")) //  Si los dos estan llenos, combinar!!
	    {
		StringTokenizer firstNames = new StringTokenizer(firstMeta.get(i).substring(19), " ");
		StringTokenizer secondNames = new StringTokenizer(secondMeta.get(i).substring(19), " ");
		ArrayList<String> combinedNames = new ArrayList<>();
		while (firstNames.hasMoreTokens())
		{
		    String name = firstNames.nextToken();
		    combinedNames.add(name);
		}
		while (secondNames.hasMoreTokens())
		{
		    String newName = secondNames.nextToken();
		    boolean addName = true;
		    for (int j = 0; j < combinedNames.size(); j++)
		    {
			if (newName.equals(combinedNames.get(j)))
			{
			    addName = false;
			}
		    }
		    if (addName)
		    {
			combinedNames.add(newName);
		    }
		} // He agregado primer todos los de la primera lista, y luego los de la segunda voy comparando para ver si existen ya
		String line = "";
		switch (i)
		{
		    case 0:
			line = ChatColor.RED + "Crafteado por:" + ChatColor.WHITE;
			break;
		    case 1:
			line = ChatColor.LIGHT_PURPLE + "Encantado por:" + ChatColor.WHITE;
			break;
		    case 2:
			line = ChatColor.GRAY + "Fusionado por:" + ChatColor.WHITE;
			break;
		    default:
			break;
		}
		for (int k = 0; k < combinedNames.size(); k++)
		{
		    line += " " + combinedNames.get(k);
		}
		newMeta.set(i, line);
	    }
	    else if (firstMeta.get(i).equals("")) // Si el primero esta vacio, entonces se llena con el segundo
	    {
		newMeta.set(i, secondMeta.get(i));
	    }
	    else if (secondMeta.get(i).equals("")) // Si el segundo esta vacio, entonces se llena con el primero
	    {
		newMeta.set(i, firstMeta.get(i));
	    }
	}
	return newMeta;
    }

}
