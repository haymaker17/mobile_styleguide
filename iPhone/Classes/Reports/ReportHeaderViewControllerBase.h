//
//  ReportHeaderViewControllerBase.h
//  ConcurMobile
//
//  Created by yiwen on 4/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportViewControllerBase.h"

@interface ReportHeaderViewControllerBase : ReportViewControllerBase 
{
    // Report Header labels and images
	UILabel			*lblName, *lblAmount, *lblLine1, *lblLine2;
    UIImageView     *img1, *img2, *img3;
}

@property (strong, nonatomic) IBOutlet UILabel			*lblName;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmount;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine1;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine2;
@property (strong, nonatomic) IBOutlet UIImageView		*img1;
@property (strong, nonatomic) IBOutlet UIImageView		*img2;
@property (strong, nonatomic) IBOutlet UIImageView		*img3;
@property (strong, nonatomic) IBOutlet UITapGestureRecognizer *rptHeaderTapGesture;

-(void)drawHeaderRpt:(id)thisObj HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
	  Image1:(UIImageView *)image1 Image2:(UIImageView *)image2 Image3:(UIImageView *)image3;

-(void)drawHeaderEntry:(EntryData *)thisEntry HeadLabel:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LabelLine1:(UILabel *)labelLine1 LabelLine2:(UILabel *)labelLine2 
                Image1:(UIImageView *)image1 Image2:(UIImageView *)image2 Image3:(UIImageView *)image3;



@end
