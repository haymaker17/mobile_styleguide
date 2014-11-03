//
//  ActiveReportListCell.h
//  ConcurMobile
//
//  Created by yiwen on 4/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SelectReportCell.h"

@interface ActiveReportListCell : SelectReportCell 
{
	UIImageView     *image1;
	UIImageView     *image2;
	UIImageView     *image3;
	UIImageView     *image4;

	UILabel			*lblRptStatus;
}

@property (nonatomic, strong) IBOutlet UIImageView	*image1;
@property (nonatomic, strong) IBOutlet UIImageView	*image2;
@property (nonatomic, strong) IBOutlet UIImageView	*image3;
@property (nonatomic, strong) IBOutlet UIImageView	*image4;
@property (nonatomic, strong) IBOutlet UILabel		*lblRptStatus;


@end
