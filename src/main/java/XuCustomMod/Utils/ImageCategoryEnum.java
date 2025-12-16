package XuCustomMod.Utils;

import XuCustomMod.XuCustomMod;

public enum ImageCategoryEnum {
    RELICS("relics");

    public final String CategoryName;

    ImageCategoryEnum(String categoryName) {
        this.CategoryName = categoryName;
    }

    public static String getImagePath(ImageCategoryEnum category, String imageName) {
        return XuCustomMod.MOD_RES_ID + "/images/" + category.CategoryName + "/" + imageName + ".png";
    }
}
