//
//  SummaryCellMLines.m
//  ConcurMobile
//
//  Created by yiwen on 4/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SummaryCellMLines.h"
#import "ImageUtil.h"

@implementation SummaryCellMLines

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
    if (self.singleSelect)
        [self selectCell:selected];
}

// Call these for tables not in editing mode
-(void)layoutWithSelect:(BOOL) bSelect
{
    if (bSelect && self.ivSelected != nil && self.ivSelected.frame.origin.x<0)
    {
        CGRect frName = self.lblName.frame;
        self.lblName.frame = CGRectMake(frName.origin.x + 38, frName.origin.y, frName.size.width, frName.size.height);
        CGRect frLbl1 = self.lblLine1.frame;
        self.lblLine1.frame = CGRectMake(frLbl1.origin.x+38, frLbl1.origin.y, frLbl1.size.width, frLbl1.size.height);
        CGRect frLbl2 = self.lblLine2.frame;
        self.lblLine2.frame = CGRectMake(frLbl2.origin.x+38, frLbl2.origin.y, frLbl2.size.width, frLbl2.size.height);
        CGRect frLbl3 = self.lblLine3.frame;
        self.lblLine3.frame = CGRectMake(frLbl3.origin.x+38, frLbl3.origin.y, frLbl3.size.width,  frLbl3.size.height);
        self.ivSelected.frame = CGRectMake(8, -10, 33, 74);
    }
    else if (!bSelect && self.ivSelected != nil && self.ivSelected.frame.origin.x>0)
    {
        CGRect frName = self.lblName.frame;
        self.lblName.frame = CGRectMake(frName.origin.x-38, frName.origin.y, frName.size.width, frName.size.height);
        CGRect frLbl1 = self.lblLine1.frame;
        self.lblLine1.frame = CGRectMake(frLbl1.origin.x-38, frLbl1.origin.y, frLbl1.size.width, frLbl1.size.height);
        CGRect frLbl2 = self.lblLine2.frame;
        self.lblLine2.frame = CGRectMake(frLbl2.origin.x-38, frLbl2.origin.y, frLbl2.size.width, frLbl2.size.height);
        CGRect frLbl3 = self.lblLine3.frame;
        self.lblLine3.frame = CGRectMake(frLbl3.origin.x-38, frLbl3.origin.y, frLbl3.size.width,  frLbl3.size.height);
        self.ivSelected.frame = CGRectMake(-60, -10, 33, 74);        
    }
}

- (void) selectCell:(BOOL) selected
{
    if (self.ivSelected != nil && self.ivSelected.frame.origin.x>0)
    {
        if (selected)
            self.ivSelected.image = [UIImage imageNamed:@"check_greenselect"]; 
        else 
            self.ivSelected.image = [UIImage imageNamed:@"check_unselect"]; 
    }    
}


#pragma mark -
#pragma mark Cell data initilation Methods 

-(void) resetCellContent:(NSString*) title withAmount:(NSString*)amt withLine1:(NSString*)line1 withLine2:(NSString*)line2 withImage1:(NSString*)imgName1 withImage2:(NSString*)imgName2 withImage3:(NSString*)imgName3
{
    self.lblName.text = title;
    self.lblAmount.text = amt;
    self.lblLine1.text = line1;
    self.lblLine2.text = line2;
    self.img1.image = [ImageUtil getImageByName:imgName1];
    self.img2.image = [ImageUtil getImageByName:imgName2];
    self.img3.image = [ImageUtil getImageByName:imgName3];

    self.lblLine3.text = @"";
    self.lblRLine1.text = @"";

}


@end
