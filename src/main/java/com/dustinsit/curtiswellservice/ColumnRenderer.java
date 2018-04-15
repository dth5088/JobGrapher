/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dustinsit.curtiswellservice;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.renderer.DocumentRenderer;
import com.itextpdf.layout.renderer.IRenderer;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dth5088
 */
public class ColumnRenderer extends DocumentRenderer {
    protected int nextAreaNumber;
    protected final Rectangle[] columns;
    protected int currentAreaNumber;
    protected Set<Integer> moveColumn = new HashSet<>();
    
    
    public ColumnRenderer(Document document, Rectangle[] columns) {
        super(document);
        this.columns = columns;
    }
    
    @Override
    protected LayoutArea updateCurrentArea(LayoutResult overflowResult) {
        if(overflowResult.getAreaBreak() != null 
                && overflowResult.getAreaBreak().getType()
                != AreaBreakType.NEXT_AREA) {
            nextAreaNumber = 0;
        }
        if(nextAreaNumber % columns.length == 0) {
            super.updateCurrentArea(overflowResult);
        }
        currentAreaNumber = nextAreaNumber + 1;
        return (currentArea = new LayoutArea(currentPageNumber,
            columns[nextAreaNumber++ % columns.length].clone()));
    }
    
    @Override
    protected PageSize addNewPage(PageSize customPageSize) {
        if(currentAreaNumber != nextAreaNumber &&
                currentAreaNumber % columns.length != 0)
            moveColumn.add(currentPageNumber -1);
        return super.addNewPage(customPageSize);
    }
    
    @Override
    protected void flushSingleRenderer(IRenderer resultRenderer) {
        int pageNum = resultRenderer.getOccupiedArea().getPageNumber();
        if(moveColumn.contains(pageNum)) {
            resultRenderer.move(columns[0].getWidth() / 2, 0);
        }
        super.flushSingleRenderer(resultRenderer);
    }
    
    
    
    
}
