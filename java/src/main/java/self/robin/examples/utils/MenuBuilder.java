package self.robin.examples.utils;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class MenuBuilder {

    private List<Menu> list = new ArrayList<>();

    public static MenuBuilder create(){
        return new MenuBuilder();
    }

    public Menu buildMenu(String name,String router, String icon){
        Menu menu = new Menu(name, router, icon);
        list.add(menu);
        return menu;
    }

    public List<Menu> builder(){
        return list;
    }

    @Data
    public static class Menu{
        List list = new ArrayList<>();

        private String name;
        private String icon;
        private String router;

        public Menu(String name,String router,String icon){
            this.name = name;
            this.icon = icon;
            this.router = router;
        }

        public Menu addItem(String name, String router, String icon){
            Item item = new Item(name, router, icon);
            list.add(item);
            return this;
        }

        public Menu addMenuItem(String name,String router, String icon){
            Menu menu = new Menu(name,router,icon);
            list.add(menu);
            return menu;
        }

    }

    @Data
    public static class Item{

        private String name;
        private String router;
        private String icon;

        public Item(String name, String router, String icon){
            this.name = name;
            this.router = router;
            this.icon = icon;
        }
    }

}
