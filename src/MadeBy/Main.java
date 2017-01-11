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
	    int nItems = 0;
	    for (int i = 1; i < is.length; i++) // Verificar cuantos aires hay en la mesa de crafteo
	    {
		if (is[i].getAmount() == 1 && is[i].getMaxStackSize() == 1)
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
		if (isRepair && firstItem.getData().getItemType().equals(secondItem.getData().getItemType())) // SI ES REPARACION
		{
		    String player = event.getView().getPlayer().getName();
		    // Primero tengo que combinar y luego añadir lo nuevo. Tanto aqui como en el anvil
		    List<String> firstMeta = firstItem.getItemMeta().getLore();
		    List<String> secondMeta = secondItem.getItemMeta().getLore();
		    List<String> loreList = combineLores(firstMeta, secondMeta); // Combino

		    loreList = addNewPlayerTo(loreList, FUSIONADO, player);       // Agrego

		    ItemStack item = event.getRecipe().getResult();
		    ItemMeta meta = item.getItemMeta();
		    meta.setLore(loreList);
		    item.setItemMeta(meta);
		    event.getInventory().setResult(item);
		}
		else
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
	    /*CraftingInventory craftingTable = (CraftingInventory) inventory;
	    ItemStack is[] = craftingTable.getContents();

	    int airs = 0;
	    for (int i = 0; i < is.length; i++) // Verificar cuantos aires hay en la mesa de crafteo
	    {
		if (is[i].getAmount() == 0)
		{
		    airs++;
		}
	    }
	    ItemStack firstItem = null;
	    ItemStack secondItem = null;
	    if (airs == 7 && is.length == 10 || airs == 2 && is.length == 5) // Significa que hay 2 objetos en la mesa. Voy a ...
	    {
		// Empiezo en int = 1 por que el 0 es el resultado
		for (int i = 1; i < is.length; i++) // ... obtener los objetos y despues ...
		{
		    if (is[i].getAmount() == 1)
		    {
			//Bukkit.broadcastMessage(i + " con " + is[i].getType().toString());
			if (firstItem == null)
			{
			    firstItem = is[i];
			}
			else
			{
			    secondItem = is[i];
			}
		    }
		}
		if (firstItem.getData().getItemType().equals(secondItem.getData().getItemType())) // ... compararlos a ver si son iguales
		{
		    isRepair = true;
		    String player = event.getView().getPlayer().getName();
		    // Primero tengo que combinar y luego añadir lo nuevo. Tanto aqui como en el anvil
		    List<String> firstMeta = firstItem.getItemMeta().getLore();
		    List<String> secondMeta = secondItem.getItemMeta().getLore();
		    List<String> loreList = combineLores(firstMeta, secondMeta); // Combino

		    loreList = addNewPlayerTo(loreList, FUSIONADO, player);       // Agrego

		    ItemStack item = event.getRecipe().getResult();
		    ItemMeta meta = item.getItemMeta();
		    meta.setLore(loreList);
		    item.setItemMeta(meta);
		    event.getInventory().setResult(item);

		}
	    }*/
	}
	/*if (!isRepair)// SI ES UN CRAFTEO NORMAL
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
	}*/
    }

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
	if (event.getInventory() instanceof AnvilInventory)
	{
	    String player = event.getWhoClicked().getName();
	    AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
	    ItemStack firstItem = anvilInventory.getItem(0);
	    ItemStack secondItem = anvilInventory.getItem(1);
	    List<String> firstLores = firstItem.getItemMeta().getLore();
	    List<String> secondLores = secondItem.getItemMeta().getLore(); // AQUI PEGA NULLPOINTER EXCEPTION
	    List<String> resultLores = combineLores(firstLores, secondLores);
	    resultLores = addNewPlayerTo(resultLores, FUSIONADO, player); //ESTA LINEA NO VA

	    ItemStack resultItem = anvilInventory.getItem(2);
	    resultItem.getItemMeta().setLore(resultLores);

	}
    }

    private List<String> verifyLoreList(List<String> loreList)
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

    public List<String> addNewPlayerTo(List<String> loreList, int loreAtt, String player)
    {
	String line = loreList.get(loreAtt); // Me tengo que asegurar antes de que exista
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
	List<String> newMeta = new ArrayList<>();
	newMeta.add("");
	newMeta.add("");
	newMeta.add("");
	for (int i = 0; i < 3; i++)
	{
	    if (firstMeta.get(i).equals("") && secondMeta.get(i).equals("")) // Si los dos estan vacios, no hay nada que conbinar
	    {
		newMeta.set(i, "");
	    }
	    else if (!firstMeta.get(i).equals("") && !secondMeta.get(i).equals("")) //  Si los dos estan llenos, combinar!!
	    {
		//Bukkit.broadcastMessage(firstMeta.get(i).substring(19));
		StringTokenizer firstNames = new StringTokenizer(firstMeta.get(i).substring(19), " ");
		StringTokenizer secondNames = new StringTokenizer(secondMeta.get(i).substring(19), " ");
		ArrayList<String> combinedNames = new ArrayList<>();
		while (firstNames.hasMoreTokens())
		{
		    String name = firstNames.nextToken();
		    Bukkit.broadcastMessage("AGREGANDO = _" + name + "_");
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
