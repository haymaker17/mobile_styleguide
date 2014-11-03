//
//  SummaryCellMLines.h
//  ConcurMobile
//
//  Created by yiwen on 4/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

// Support SummaryCell4Lines.xib, SummaryCell3Lines.xib and SummaryCell2Lines.xib
@interface SummaryCellMLines : UITableViewCell {

}

@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UILabel *lblAmount;
@property (nonatomic, strong) IBOutlet UILabel *lblAmtValue;
@property (nonatomic, strong) IBOutlet UILabel *lblLine1;
@property (nonatomic, strong) IBOutlet UILabel *lblLine2;
@property (nonatomic, strong) IBOutlet UILabel *lblRLine1;
@property (nonatomic, strong) IBOutlet UILabel *lblPmtType;
@property (nonatomic, strong) IBOutlet UILabel *lblLine3;

@property (nonatomic, strong) IBOutlet UIImageView		*img1;
@property (nonatomic, strong) IBOutlet UIImageView		*img2;
@property (nonatomic, strong) IBOutlet UIImageView		*img3;
@property (nonatomic, strong) IBOutlet UIImageView		*ivSelected;
@property (nonatomic, strong) IBOutlet UIImageView      *ivException;
@property BOOL singleSelect;

-(void) resetCellContent:(NSString*) name withAmount:(NSString*)amt withLine1:(NSString*)line1 withLine2:(NSString*)line2 withImage1:(NSString*)imgName1 withImage2:(NSString*)imgName2 withImage3:(NSString*)imgName3;

-(void)layoutWithSelect:(BOOL) bSelect;
- (void) selectCell:(BOOL) selected;

@end
