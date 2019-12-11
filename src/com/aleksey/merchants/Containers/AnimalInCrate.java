package com.aleksey.merchants.Containers;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;



/**
 *
 * @author Swarg
 * for Compare Crate with animal 
 * "taeog.animalcrate.item.ItemCrate"
 */
public class AnimalInCrate {    
    
    public static final String ANIMAL = "Animal";
    public static final String ID = "id";
    public static final String SEX = "Sex";
    public static final String AGE = "Age";        
    public static final String MSPEED = "MateSpeed";
    public static final String MJUMP = "MateJump";
    //public static final String MHEALTH = "MateHealth";? Health
    public static final String VARIANT = "Variant";
    public static final String FAMILIARITY = "Familiarity";//35 cap
    public static final String ATTRIBUTES = "Attributes";    
    public static final String GMAXHEALTH = "generic.maxHealth";
    public static final String GMOVEMENTSPEED = "generic.movementSpeed";
    public static final String GJUMPSTRENGTH = "horse.jumpStrength";
    public static boolean allowSetOnlyTFCAnimal = true;
    public static String TFCAnimalsList = "";
    public static final String[] InvalidTFCAnimal = {"skeletonTFC","zombieTFC","spiderTFC","slimeTFC","ghastTFC","caveSpiderTFC","blazeTFC", "endermanTFC","pigZombieTFC","boarTFC","banditTFC","minecartTFC","arrowTFC","standTFC","creeperTFC","irongolemTFC"};
   // public static final String[] sexx= {" ","\u2642", "\u2640" };
    
    //public static final String  = "";
    public int id;
    public String name;
    public int sex;//+1
    public int age;
    public float speed;
    private int speedX10; //123 is 12.3 m/s
    public int jumpHX10;//25  is 2.5m jumpHeigth*10
    public float jumpStrength;
    public int health;
    public int familiarity;
    public int variant;
    
    public static boolean isValidAnimalCrate(ItemStack stack)
    {
      return ( stack!=null && stack.getItem().getClass().toString().contains("taeog.animalcrate.item.ItemCrate"));
    }
    /**
     * Create by AnimalCrateItemStack NBT
     */ 
    public AnimalInCrate(NBTTagCompound nbt)
    {
        this.id = 0;
        this.sex = 0;
        this.age = 0;
        this.speed = 0;
        this.jumpStrength = 0;        
        this.speedX10 = 0;
        this.jumpHX10 = 0;        
        this.familiarity = 0;
        this.variant = 0;        
        
        if (nbt == null)
            return;
        
        nbt = nbt.getCompoundTag(ANIMAL);
        if (nbt == null)
            return;
        
        name = nbt.getString(ID);//horseTFC
        id = getIdByAnimalName(name);
        if ( nbt.hasKey(SEX) )
                sex = nbt.getInteger(SEX) + 1; //1 man 2 female   0 - error     
        
        age = nbt.getInteger(AGE);
        age = (age <= 0 ) ? 0 : age;
            
        variant = nbt.getInteger(VARIANT);//surface
        familiarity = nbt.getInteger(FAMILIARITY);//35 cap
        
        NBTTagList anbt = nbt.getTagList(ATTRIBUTES, 10);
        if( anbt == null || anbt.tagCount() < 1)
            return;
        
        for (int i = 0; i < anbt.tagCount(); i++) 
        {
            NBTTagCompound att = anbt.getCompoundTagAt(i);
            String _name = att.getString("Name");            
            
            if (_name != null && _name.contains(GMAXHEALTH) )
                health = (int) att.getDouble("Base"); 
            if (_name != null && _name.contains(GMOVEMENTSPEED) )
                speed = (float) att.getDouble("Base");
            else if (_name != null && _name.endsWith(GJUMPSTRENGTH))
            {
                jumpStrength = (float) att.getDouble("Base");
                if (speed > 0)
                    break;                
            }    
        }   
    }
    
    /**
     * Create AnimCrate to compare on payMode by GUI params
     */
    public AnimalInCrate(
            int p1,  //id
            int p2,  //familiarity * 10 + sex;
            int p3,  //speed + jump  * 1000;
            int p4   //a.variant;
    )
    {
        this.id = 0;
        this.sex = 0;
        this.age = 0;
        this.speed = 0;
        this.jumpStrength = 0;        
        this.speedX10 = 0;
        this.jumpHX10 = 0;        
        this.familiarity = 0;
        this.variant = 0;        
        
        if (p1 <= 0) {
            return;
        }
        this.id = p1;
        this.sex = p2 % 10;
        //sex = ( sex > 0)? sex - 1: 0;// 1 man 2 female +1 by gamestandart        
        this.familiarity = (p2 >= 10)? (int) Math.floor( p2 / 10 ):0;
        
        this.speedX10 = (p3 > 0)? p3 % 1000: 0;
        this.speed = (this.speedX10 > 0) ? (float) this.speedX10 / 430 : 0 ;
        
        this.jumpHX10 = (p3 > 1000)? (int) Math.floor(p3 / 1000): 0;
        this.jumpStrength = (float) getJumpStrength(jumpHX10);
        
        this.variant = p4;
    } 
    
    
    /**
     * For GUI setPayitem
     * @return 
     */
    public NBTTagCompound writeToNBT()
    {
        if ( this.id <= 0 )
            return null;
        
        NBTTagCompound nbt = new NBTTagCompound();
        String name = getAnimalNameByID(id);        
        if ( name == null || name.isEmpty() || 
                //can set only valid tfc animal
                ( allowSetOnlyTFCAnimal && !isValidTFCAnimal(name) ) )
            return null;
            
        nbt.setString(ID, name);        
        nbt.setInteger(SEX, (sex > 0) ? sex-1 : 0 );//1 man 2 female
        nbt.setInteger(AGE, age);
        nbt.setInteger(VARIANT, variant);
        nbt.setInteger(FAMILIARITY,familiarity);
        
        if ( this.speed > 0 )//("horseTFC")
        {
            NBTTagList attrList = new NBTTagList();//nbt.getTagList(ATTRIBUTES, 10);
            
            NBTTagCompound attrSpeed = new NBTTagCompound();            
            attrSpeed.setFloat("Base", this.speed );
            attrSpeed.setString("Name",GMOVEMENTSPEED);        
            attrList.appendTag(attrSpeed);
            
            NBTTagCompound attrJump = new NBTTagCompound();            
            if ( this.jumpStrength > 0 )
            {
                attrJump.setFloat("Base", this.jumpStrength );
                attrJump.setString("Name",GJUMPSTRENGTH);
                attrList.appendTag(attrJump);                
            }
            nbt.setTag(ATTRIBUTES, attrList);
        }
        NBTTagCompound anim = new NBTTagCompound();
        anim.setTag(ANIMAL, nbt);
        return anim;    
    }
    
    /**
     * game inside value to m/s *10 
     * @return 121 is 12.1m/s
     */
    public int getSpeedX10()
    {
       if (this.speedX10==0)
           this.speedX10 = (int) Math.floor( this.speed * 430 );
       
        return  this.speedX10;
    }
    /**
     * game jumpStreght value to Height in m * 10
     * @return 25 is 2.5 m
     */        
    public int getJumpHX10()
    {
        if (jumpHX10==0)
            jumpHX10 = (int) Math.floor( getJumpHeight(this.jumpStrength) * 10);
        
        return jumpHX10;
    }
    
    /**
     * Suited enimal to trade. True if animal two have better feature 
     * @param a animal two
     * @return 
     */
    public boolean isAnimalEqual(AnimalInCrate a)
    {
        //MateVariant:0,Variant:768 внешний вид
        if ( a ==null || this.id==0)
            return false;
        
        return ( this.id == a.id 
                //if the value of sex is not defined(0) allow trade animal with any sex
                && ( this.sex==0 || this.sex == a.sex  )
                
                && (this.speed==0 || this.speed <= a.speed)
                && (this.jumpStrength==0 || this.jumpStrength <= a.jumpStrength)
                && (this.age==0 || this.age <= a.age)
                && (this.familiarity==0 || this.familiarity <= a.familiarity)
                );        
    }
    
    
    public static int getIdByAnimalName(String name)
    {
        if (name == null || name.isEmpty()  )
            return 0;
        //EntityList.classToStringMapping.containsKey(ids)
        Class oclass = (Class)EntityList.stringToClassMapping.get(name);
        if (oclass == null)
            return 0;
        Set set = EntityList.IDtoClassMapping.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();                  
            if (mentry.getValue()==oclass) {
                return (Integer) mentry.getKey();                    
            }    
        }            
        return 0;
    }
    
    public static String getAnimalNameByID(int id)
    {
        if (id<=0)
            return "";
        Class oclass = (Class) EntityList.IDtoClassMapping.get(Integer.valueOf(id));
        //EntityList.classToStringMapping.containsKey(ids)
        //Class oclass = (Class)stringToClassMapping.get(name);
        if (oclass == null)
            return "";
        return (String) EntityList.classToStringMapping.get(oclass);        
    }
    
    //from https://github.com/fubira/HorseInfoReloaded/blob/master/src/main/java/net/ironingot/horseinfo/HorseInfoUtil.java
    public static double getJumpHeight(double jumpStrength) {
        double yVelocity = jumpStrength;
        double jumpHeight = 0.0d;

        while (yVelocity > 0.0D) {
            jumpHeight += yVelocity;
            yVelocity -= 0.08D;
            yVelocity *= 0.98D;
        }
        return Math.floor(jumpHeight * 10.0D) / 10.0D ;
    }
    /**
     * back getJumpHeight jump Height to Strength
     * @param jumpHeight from GUI *10 like 55 is 5.5m;  floor 5
     * @return JumpStrength
     */
    public static double getJumpStrength(int jumpHeightX10)
    {
        if (jumpHeightX10 == 0)
            return 0;
        
        float[] strength = new float[] { 
            0.0f,    // 0  0 
            0.2490f, // 1  0.5  05
            0.3690f, // 2  1.0  10
            0.4640f, // 3  1.5  15
            0.5450f, // 4  2.0  20
            0.6180f, // 5  2.5
            0.6850f, // 6  3.0
            0.7460f, // 7  3.5
            0.8040f, // 8  4.0
            0.8580f, // 9  4.5
            0.9100f, //10  5.0
            0.9600f, //11  5.5
            1.0080f // 12  6.0
        };
        int d = jumpHeightX10 / 5;
        return (d < 1 || d >= strength.length)? 0: strength[d];        
    }
    
    public static String getListOfAnimals()
    {        
        if ( TFCAnimalsList==null || TFCAnimalsList.isEmpty() )
        {
            int h = EntityList.IDtoClassMapping.size();
            String r = "";
            for (int i = 0; i < h; i++) {
                String name = getAnimalNameByID(i);
                if ( isValidTFCAnimal(name) )
                      r += String.format("%s  %s\n", Integer.toString(i), name);
                
            }
            TFCAnimalsList = r; 
        }
        return TFCAnimalsList;
    }
    
    public static boolean isValidTFCAnimal(String name)
    {
        if (name==null || name.isEmpty() || ! name.contains("TFC"))
            return false;
        
        for (int i = 0; i < InvalidTFCAnimal.length; i++) 
        {
            String badName = InvalidTFCAnimal[i];
            if (name.contains(badName))
                return false;            
        }        
        return true;
    } 
    
    
}
