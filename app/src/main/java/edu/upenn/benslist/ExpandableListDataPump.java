package edu.upenn.benslist;

/**
 * Created by johnquinn on 2/15/17.
 */
/*
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandableListDataPump {

    public static HashMap<String, List<String>> getFurnitureData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> color = new ArrayList<String>();
        color.add("Blue");
        color.add("Tan");
        color.add("Black");
        color.add("White");
        color.add("Grey");

        List<String> fabric = new ArrayList<String>();
        fabric.add("Leather");
        fabric.add("Linen");
        fabric.add("Vinyl");
        fabric.add("Cotton");
        fabric.add("Other");

        List<String> type = new ArrayList<String>();
        type.add("Couch");
        type.add("Sofa");
        type.add("Recliner");
        type.add("Chair");
        type.add("Bed");
        type.add("Other");


        expandableListDetail.put("Color", color);
        expandableListDetail.put("Material", fabric);
        expandableListDetail.put("Type of Furniture", type);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getElectronicsData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> type = new ArrayList<String>();
        type.add("Headphones");
        type.add("Speakers");
        type.add("Computers");
        type.add("Smartphones & Tablets");
        type.add("Other");

        expandableListDetail.put("Type of Electronics", type);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getBooksData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> genre = new ArrayList<String>();
        genre.add("Fiction");
        genre.add("Non-fiction");
        genre.add("Science Fiction");
        genre.add("Fantasy");
        genre.add("Mystery");
        genre.add("Poetry");
        genre.add("Horror");
        genre.add("Other");

        List<String> textbook = new ArrayList<String>();
        textbook.add("History");
        textbook.add("Biology");
        textbook.add("Chemistry");
        textbook.add("Physics");
        textbook.add("Math");
        textbook.add("Business/Economics");
        textbook.add("Philosophy");
        textbook.add("Psychology");
        textbook.add("Sociology");
        textbook.add("Language");
        textbook.add("Other");

        List<String> condition = new ArrayList<String>();
        condition.add("New");
        condition.add("Used");

        List<String> term = new ArrayList<String>();
        term.add("Buy");
        term.add("Rent");

        expandableListDetail.put("Genre", genre);
        expandableListDetail.put("Textbook", textbook);
        expandableListDetail.put("Condition", condition);
        expandableListDetail.put("Term", term);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getKitchenSuppliesData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> type = new ArrayList<String>();
        type.add("Microwave");
        type.add("Refrigerator");
        type.add("Eating Utensils");
        type.add("Cooking Utensils");
        type.add("Blender");
        type.add("Oven");
        type.add("Stove");
        type.add("Dishwasher");
        type.add("Toaster");
        type.add("Coffee Maker");
        type.add("Other");

        expandableListDetail.put("Type", type);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getClothesData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> gender = new ArrayList<String>();
        gender.add("Male");
        gender.add("Female");

        List<String> type = new ArrayList<String>();
        type.add("T-Shirts");
        type.add("Long Sleeve Shirts");
        type.add("Crewneck Sweatshirts");
        type.add("Hooded Sweatshirts");
        type.add("Pants");
        type.add("Shorts");
        type.add("Socks");
        type.add("Shoes");
        type.add("Underwear");
        type.add("Jackets");
        type.add("Other");

        List<String> color = new ArrayList<String>();
        color.add("Red");
        color.add("Orange");
        color.add("Yellow");
        color.add("Green");
        color.add("Blue");
        color.add("Purple");
        color.add("Tan");
        color.add("Black");
        color.add("White");
        color.add("Grey");

        List<String> material = new ArrayList<String>();
        material.add("Cotton");
        material.add("Silk");
        material.add("Wool");
        material.add("Denim");
        material.add("Leather");
        material.add("Nylon");
        material.add("Polyester");
        material.add("Spandex");
        material.add("Other");


        List<String> condition = new ArrayList<String>();
        condition.add("New");
        condition.add("Used");


        expandableListDetail.put("Gender", gender);
        expandableListDetail.put("Type", type);
        expandableListDetail.put("Color", color);
        expandableListDetail.put("Material", material);
        expandableListDetail.put("Condition", condition);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getServicesData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> type = new ArrayList<String>();
        type.add("Relationship/Dating");
        type.add("Cleaning");
        type.add("Delivery");
        type.add("Personal/Sports Training");
        type.add("Music Lessons");
        type.add("Babysitter");
        type.add("Other");

        expandableListDetail.put("Type", type);
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    public static HashMap<String, List<String>> getOtherData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
        addLocationAndPrice(expandableListDetail);
        return expandableListDetail;
    }

    private static void addLocationAndPrice(HashMap<String, List<String>> listDetails) {
        List<String> price = new ArrayList<String>();
        price.add("$0 - $49.99");
        price.add("$50 - $99.99");
        price.add("$100 - $149.99");
        price.add("$150 - $199.99");
        price.add("$200 - $249.99");
        price.add("$250 - $299.99");
        price.add("$300 - $399.99");
        price.add("$400 - $499.99");
        price.add("$500+");

        List<String> location = new ArrayList<String>();
        location.add("0 - 10 miles");
        location.add("10 - 20 miles");
        location.add("20 - 30 miles");
        location.add("30 - 40 miles");
        location.add("40 - 50 miles");
        location.add("50 - 75 miles");
        location.add("75 - 100 miles");
        location.add("100+ miles");

        listDetails.put("Price", price);
        listDetails.put("Location", location);
    }

}
*/