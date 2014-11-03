//
//  ReportEntryItemizedCell.h
//  ConcurMobile
//
//  Created by yiwen on 1/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ReportEntryItemizedCell : UITableViewCell {

	UILabel			*lblItemized;
	UILabel			*lblRemaining;
	UILabel			*lblItemizedAmt;
	UILabel			*lblRemainingAmt;

}

@property (nonatomic, strong) IBOutlet UILabel		*lblItemized;
@property (nonatomic, strong) IBOutlet UILabel		*lblRemaining;
@property (nonatomic, strong) IBOutlet UILabel		*lblItemizedAmt;
@property (nonatomic, strong) IBOutlet UILabel		*lblRemainingAmt;

@end
