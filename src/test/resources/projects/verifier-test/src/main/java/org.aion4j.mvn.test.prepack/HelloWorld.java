package org.aion4j.mvn.test.prepack;


import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;

import java.math.BigInteger;
import java.util.Comparator;

public class HelloWorld {

    //variable initializations
    public static Address owner;
    public static Address newOwner;
    public static Address contractAddress;
    public static long timestamp;
    public static long blockNumber;



    //In Solidity: conmyStructor


    //Solidity: modifier
    private static void onlyOwner() {
        BlockchainRuntime.require(BlockchainRuntime.getCaller().equals(owner));
    }





    //Solidity: MyStruct
    public static class MyStruct {
        int id;
        boolean valid;
    }

    //Solidity: mapping
    public static final AionMap<Address, MyStruct> myMap = new AionMap<>();
    public static final AionSet<Address> mySet = new AionSet<>();
    public static final AionList<Address> myList = new AionList<>();


    //In Solidy: consturctor
    static{
        contractAddress = BlockchainRuntime.getAddress();
        owner = BlockchainRuntime.getCaller();
        timestamp = BlockchainRuntime.getBlockTimestamp();
        blockNumber = BlockchainRuntime.getBlockNumber();
    }





    //solidity doesnt support try/catch & decimals, double, floats, BigDecimals
    public static float floatDivision(float dividend, float divisor){
        try{
            return divisionException(dividend, divisor); //nrgUsed=73949
        } catch (IllegalArgumentException ex) {
            BlockchainRuntime.println(ex.getMessage());
            return Float.POSITIVE_INFINITY; //nrgUsed=67140
        }
    } //nrgUsed=73949

    //try catch decimal numbers tested!
    public static float divisionException(float dividend, float divisor) {
        if(divisor == 0) {
            throw new IllegalArgumentException("Divisor cannot be ZERO!");//nrgUsed=67140
        }
        BlockchainRuntime.println("The result is: " + dividend/divisor);
        return dividend/divisor; //nrgUsed=73635
    }

    //In Solidity: selfdestruct(address)
    public static void destructTheContract(Address beneficiary) {
        onlyOwner();
        BlockchainRuntime.selfDestruct(beneficiary);
    } //nrgUsed=72082

   /* public static void sortList() {
        AionList<String> list = new AionList<>();
        list.add("AVM");
        list.add("is");
        list.add("awesome");
        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }*/

    //Solidity: address.transfer  ->value
    public static void transfer(Address to, long value) {
        onlyOwner();
        Result result = BlockchainRuntime.call(to, BigInteger.valueOf(value), new byte[0] , BlockchainRuntime.getRemainingEnergy());
        if (result.isSuccess()) {
            BlockchainRuntime.println("Transfer succeeded. " + BlockchainRuntime.getBalance(to) + " " + BlockchainRuntime.getBalanceOfThisContract());
        } else {
            BlockchainRuntime.println("Transfer failed.");
        }
    }

    //Solidity: address.call().value().gas()
    public static void call(Address to, long value, byte[] data, long energyLimit) {
        onlyOwner();
        BlockchainRuntime.call(to, BigInteger.valueOf(value), data, energyLimit); //nrgUsed=68445
    }


    public static void callAnotherContract(Address toContractAddress) {
        byte[] data = ABIEncoder.encodeMethodArguments("callThisFunction");
        BlockchainRuntime.call(toContractAddress, BigInteger.valueOf(0), data , BlockchainRuntime.getRemainingEnergy());
    }

    public static void callAnotherContractWithArg(Address toContractAddress /*
    0x0ffbb6ea1c53f1b7fe028404c110be12e3e0105001a5697d52c222fda9a39219*/, String newString) {
        byte[] data = ABIEncoder.encodeMethodArguments("changeSomething", newString);
        BlockchainRuntime.call(toContractAddress, BigInteger.valueOf(0), data , BlockchainRuntime.getRemainingEnergy());
        //emitEvent();
    }

    //In Solidity:  address newContract = new Contract(data)
    public static Address createNewContract(byte[] dappData) {
        Result createResult = BlockchainRuntime.create(BigInteger.ZERO,dappData,BlockchainRuntime.getRemainingEnergy());
        if (!createResult.isSuccess()) {
            BlockchainRuntime.revert(); //nrgUsed=1970189
        }
        Address newAddress = new Address(createResult.getReturnData());
        return newAddress;
    }   //nrgUsed=1989185





    public static void transferOwnership(Address newOwnerAddress) {
        onlyOwner();
        newOwner = newOwnerAddress;
    }


    public static void acceptOwnership() {
        BlockchainRuntime.require(BlockchainRuntime.getCaller().equals(newOwner));
        owner = newOwner;
        newOwner = null;
    }




    //cannot print debug messages in solidity
    public static void printValues() {

        BlockchainRuntime.println("Contract Address: " + contractAddress);

        long contractBalance = getContractBalance();
        BlockchainRuntime.println("Contract Balance: " + contractBalance);
        BlockchainRuntime.println("Contract Balance: " + BlockchainRuntime.getBalanceOfThisContract());


        BlockchainRuntime.println("Contract owner: " + owner);
        BlockchainRuntime.println("Contract owner balance: " + BlockchainRuntime.getBalance(owner));

        BlockchainRuntime.println("Timestamp: " + timestamp);
        BlockchainRuntime.println("Block Number: " + blockNumber);
        //energy cost: 124413
        //checked
    }


    //To java devs: We cannot return BigInteger in ABI ->java dev
    private static long getContractBalance() {
        return BlockchainRuntime.getBalanceOfThisContract().longValue();
        //nrgUsed=65552
        //
    }

    //Solidity: Event
    public static void newElementAdded(Address address) {
        String topic = "new address added to the map";
        BlockchainRuntime.log(topic.getBytes(), address.toString().getBytes());
    }

    public static void addElementToMap(Address address) {
        onlyOwner();
        BlockchainRuntime.require(!myMap.containsKey(address));
        //In Solidity you cannot check if a key is existed in a mapping or not
        MyStruct newElement = new MyStruct();
        newElement.id = myMap.size(); //in Solidity, you cant get a length/size for mapping
        newElement.valid = true;
        myMap.put(address, newElement);
        mySet.add(address); //write in dynamic array in solidity is more expensive
        myList.add(address);
        newElementAdded(address);
    }//nrgUsed=144162


    //In Solidity: we are able to return multiple values
    //In Java: return objects for multiple values
    public static void printElementInformation(Address address) {
        BlockchainRuntime.require(myMap.containsKey(address));
        MyStruct element;
        element = getElementInformation(address);
        BlockchainRuntime.println("Address is " + address + ".\nID is " + element.id + ". Valid is: " + element.valid);
    } //nrgUsed=111939

    private static MyStruct getElementInformation(Address address) {
        MyStruct myStruct = new MyStruct();
        myStruct.id = myMap.get(address).id;
        myStruct.valid = myMap.get(address).valid;
        return myStruct; //no returning multiple values in java
    }//checked



    //In Solidity: you cannot return a mapping
    public static AionMap getMap(){
        BlockchainRuntime.require(!myMap.isEmpty()); //nrgUsed=65981
        for (Address a : mySet){
            BlockchainRuntime.println("No. " + myMap.get(a).id + "; Address: " + a + " -> Status: " + myMap.get(a).valid + ".\n");
        }
        return myMap;
    }//nrgUsed=144162

    //In Solidity: myMap[]
    public static void updateElementInMap(Address address, boolean valid) {
        BlockchainRuntime.require(myMap.containsKey(address));
        myMap.get(address).valid = valid;
    } //nrgUsed=110781


    //In Solidity: delede map[_address]
    public static void removeElementFromMap(Address address) {
        BlockchainRuntime.require(myMap.containsKey(address));
        myMap.remove(address);
        mySet.remove(address);
        myList.remove(address);
        BlockchainRuntime.require(!myMap.isEmpty());
        updateMapId();
    }

    private static void updateMapId() {
        int id = 0;
        for (Address a : myList){
            MyStruct updatedAddressEntry = new MyStruct();
            myMap.get(a).id = id;
            id ++;
        }
    }

//list



}



