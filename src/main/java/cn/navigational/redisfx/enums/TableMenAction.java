package cn.navigational.redisfx.enums;

/**
 * {@link cn.navigational.redisfx.controller.RedisMainViewController} 表菜单动作
 *
 * @author yangkui
 * @since 1.0
 */
public enum TableMenAction {
    /**
     * 打开连接
     */
    OPEN("打开连接"),
//    /**
//     * 编辑连接信息
//     */
//    EDIT("编辑连接"),
    /**
     * 删除连接
     */
    DELETE("删除连接");

    private final String name;

    TableMenAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
