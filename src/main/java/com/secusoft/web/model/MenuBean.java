package com.secusoft.web.model;

import java.util.List;

public class MenuBean {
    private String uuid;
    private String parentPermissionUuid;
    private String name;
    private String permissionValue;
    private String relationUrl;
    private String remark;
    private String type;
    private List<Object> dataAccessRules;
    private Integer displayOrder;
    private List<Object> children;

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getParentPermissionUuid() { return parentPermissionUuid; }

    public void setParentPermissionUuid(String parentPermissionUuid) { this.parentPermissionUuid = parentPermissionUuid; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getPermissionValue() { return permissionValue; }

    public void setPermissionValue(String permissionValue) { this.permissionValue = permissionValue; }

    public String getRelationUrl() { return relationUrl; }

    public void setRelationUrl(String relationUrl) { this.relationUrl = relationUrl; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public List<Object> getDataAccessRules() { return dataAccessRules; }

    public void setDataAccessRules(List<Object> dataAccessRules) { this.dataAccessRules = dataAccessRules; }

    public Integer getDisplayOrder() { return displayOrder; }

    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public List<Object> getChildren() { return children; }

    public void setChildren(List<Object> children) { this.children = children; }
}
