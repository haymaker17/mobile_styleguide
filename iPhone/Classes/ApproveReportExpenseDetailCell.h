//
//  ApproveReportExpenseCell.h
//  ConcurMobile
//
//  Created by Yuri on 2/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class RootViewController;

@interface ApproveReportExpenseDetailCell : UITableViewCell {
    
	UILabel *labelText;
	UILabel *labelDetail;
}

@property (nonatomic, retain) IBOutlet UILabel *labelText;
@property (nonatomic, retain) IBOutlet UILabel *labelDetail;

- (NSString *) reuseIdentifier;

@end
