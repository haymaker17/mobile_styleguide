//
//  DrillCell.h
//  ConcurMobile
//
//  Created by yiwen on 4/28/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface DrillCell : UITableViewCell 
{
    UILabel			*lblName;
    UIImageView		*imgIcon;
}

@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UIImageView *imgIcon;

-(void) resetCellContent:(NSString*) name withImage:(NSString*)imgName;
+(UITableViewCell *) makeDrillCell:(UITableView*)tblView withText:(NSString*)command withImage:(NSString*)imgName enabled:(BOOL)flag;

@end
