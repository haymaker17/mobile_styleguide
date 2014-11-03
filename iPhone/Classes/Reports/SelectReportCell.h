//
//  SelectReportCell.h
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportData.h"

@interface SelectReportCell : UITableViewCell {
	UILabel			*lblName, *lblAmount, *lblDate;
	ReportData		*rpt;
}

@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UILabel *lblAmount;
@property (nonatomic, strong) IBOutlet UILabel *lblDate;
@property (nonatomic, strong) ReportData		*rpt;

@end
