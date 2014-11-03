//
//  ReportApprovalListCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportData.h"

@interface ReportApprovalListCell : UITableViewCell 
{
	UILabel			*lblName, *lblAmount, *lblLine1, *lblLine2;
	ReportData		*rpt;
	UIImageView		*img1, *img2, *img3, *img4, *img5, *img6;
}

@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UILabel *lblAmount;
@property (nonatomic, strong) IBOutlet UILabel *lblLine1;
@property (nonatomic, strong) IBOutlet UILabel *lblLine2;
@property (nonatomic, strong) ReportData		*rpt;

@property (nonatomic, strong) IBOutlet UIImageView		*img1;
@property (nonatomic, strong) IBOutlet UIImageView		*img2;
@property (nonatomic, strong) IBOutlet UIImageView		*img3;
@property (nonatomic, strong) IBOutlet UIImageView		*img4;
@property (nonatomic, strong) IBOutlet UIImageView		*img5;
@property (nonatomic, strong) IBOutlet UIImageView		*img6;

-(void) clearAllImagesInCell;
-(void) setImageByPosition:(int)imagePos imageName:(NSString *)imgName;

@end
