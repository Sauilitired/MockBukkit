package be.seeseemelk.mockbukkit.inventory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import be.seeseemelk.mockbukkit.UnimplementedOperationException;

public class PlayerInventoryMock implements PlayerInventory
{
	public static final int HOTBAR = 0;
	public static final int MAIN_INVENTORY = 9;
	public static final int BOOTS = 36;
	public static final int LEGGINGS = 37;
	public static final int CHESTPLATE = 38;
	public static final int HELMET = 39;
	
	private final ItemStack[] items = new ItemStack[getSize()];
	private final String name;
	
	public PlayerInventoryMock(String name)
	{
		this.name = name;
	}
	
	/**
	 * Asserts that a certain condition is true for all items, even {@code nulls},
	 * in this inventory.
	 * 
	 * @param condition
	 *            The condition to check for.
	 */
	public void assertTrueForAll(Function<ItemStack, Boolean> condition)
	{
		for (ItemStack item : items)
		{
			assertTrue(condition.apply(item));
		}
	}
	
	/**
	 * Assets that a certain condition is true for all items in this inventory that
	 * aren't null.
	 * 
	 * @param condition
	 *            The condition to check for.
	 */
	public void assertTrueForNonNulls(Function<ItemStack, Boolean> condition)
	{
		assertTrueForAll(itemstack -> itemstack == null || condition.apply(itemstack));
	}
	
	/**
	 * Asserts that a certain condition is true for at least one item in this
	 * inventory. It will skip any null items.
	 * 
	 * @param condition
	 *            The condition to check for.
	 */
	public void assertTrueForSome(Function<ItemStack, Boolean> condition)
	{
		for (ItemStack item : items)
		{
			if (item != null && condition.apply(item))
			{
				return;
			}
		}
		fail("Condition was not met for any items");
	}
	
	/**
	 * Asserts that the inventory contains at least one itemstack that is compatible
	 * with the given itemstack.
	 * 
	 * @param item The itemstack to compare everything to.
	 */
	public void assertContainsAny(ItemStack item)
	{
		assertTrueForSome(itemstack -> item.isSimilar(itemstack));
	}
	
	/**
	 * Asserts that the inventory contains at least a specific amount of items that are compatible
	 * with the given itemstack.
	 * @param item The itemstack to search for.
	 * @param amount The minimum amount of items that one should have.
	 */
	public void assertContainsAtLeast(ItemStack item, int amount)
	{
		int n = getNumberOfItems(item);
		String message = String.format("Inventory contains only <%d> but expected at least <%d>", n, amount);
		assertTrue(message, n >= amount);
	}
	
	/**
	 * Get the number of times a certain item is in the inventory.
	 * @param item The item to check for.
	 * @return The number of times the item is present in this inventory.
	 */
	public int getNumberOfItems(ItemStack item)
	{
		int amount = 0;
		for (ItemStack itemstack : items)
		{
			if (itemstack != null && item.isSimilar(itemstack))
			{
				amount += itemstack.getAmount();
			}
		}
		return amount;
	}
	
	@Override
	public int getSize()
	{
		return 40;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public ItemStack getItem(int index)
	{
		return items[index];
	}
	
	@Override
	public void setItem(int index, ItemStack item)
	{
		items[index] = item.clone();
	}
	
	/**
	 * Adds a single item to the inventory. Returns whatever item it couldn't add.
	 * 
	 * @param item
	 *            The item to add.
	 * @return The remaining stack that couldn't be added. If it's empty it just
	 *         returns {@code null}.
	 */
	public ItemStack addItem(ItemStack item)
	{
		item = item.clone();
		for (int i = 0; i < items.length; i++)
		{
			ItemStack oItem = items[i];
			if (oItem == null)
			{
				int toAdd = Math.min(item.getAmount(), item.getMaxStackSize());
				items[i] = item.clone();
				items[i].setAmount(toAdd);
				item.setAmount(item.getAmount() - toAdd);
			}
			else if (item.isSimilar(oItem) && oItem.getAmount() < oItem.getMaxStackSize())
			{
				int toAdd = Math.min(item.getAmount(), item.getMaxStackSize() - oItem.getAmount());
				oItem.setAmount(oItem.getAmount() + toAdd);
				item.setAmount(item.getAmount() - toAdd);
			}
			
			if (item.getAmount() == 0)
			{
				return null;
			}
		}
		
		return item;
	}
	
	@Override
	public HashMap<Integer, ItemStack> addItem(ItemStack... items) throws IllegalArgumentException
	{
		HashMap<Integer, ItemStack> notSaved = new HashMap<Integer, ItemStack>();
		for (int i = 0; i < items.length; i++)
		{
			ItemStack item = items[i];
			ItemStack left = addItem(item);
			if (left != null)
			{
				notSaved.put(i, left);
			}
		}
		return notSaved;
	}
	
	@Override
	public ItemStack[] getContents()
	{
		return items;
	}
	
	@Override
	public void setContents(ItemStack[] items) throws IllegalArgumentException
	{
		for (int i = 0; i < getSize(); i++)
		{
			if (i < items.length)
			{
				this.items[i] = items[i].clone();
			}
			else
			{
				this.items[i] = null;
			}
		}
	}
	
	@Override
	public int getMaxStackSize()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setMaxStackSize(int size)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack[] getStorageContents()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setStorageContents(ItemStack[] items) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public boolean contains(int materialId)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean contains(Material material) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean contains(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean contains(int materialId, int amount)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean contains(Material material, int amount) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean contains(ItemStack item, int amount)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public boolean containsAtLeast(ItemStack item, int amount)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public HashMap<Integer, ? extends ItemStack> all(int materialId)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public HashMap<Integer, ? extends ItemStack> all(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public int first(int materialId)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public int first(Material material) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public int first(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public int firstEmpty()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void remove(int materialId)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void remove(Material material) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void remove(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void clear(int index)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public List<HumanEntity> getViewers()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public String getTitle()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public InventoryType getType()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ListIterator<ItemStack> iterator()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ListIterator<ItemStack> iterator(int index)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public Location getLocation()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack[] getArmorContents()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack[] getExtraContents()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack getHelmet()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack getChestplate()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack getLeggings()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public ItemStack getBoots()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setArmorContents(ItemStack[] items)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void setExtraContents(ItemStack[] items)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void setHelmet(ItemStack helmet)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void setChestplate(ItemStack chestplate)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void setLeggings(ItemStack leggings)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public void setBoots(ItemStack boots)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public ItemStack getItemInMainHand()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setItemInMainHand(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public ItemStack getItemInOffHand()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setItemInOffHand(ItemStack item)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public ItemStack getItemInHand()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setItemInHand(ItemStack stack)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public int getHeldItemSlot()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public void setHeldItemSlot(int slot)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
		
	}
	
	@Override
	public int clear(int id, int data)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
	@Override
	public HumanEntity getHolder()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}
	
}
