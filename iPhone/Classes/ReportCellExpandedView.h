//
//  ReportCellExpandedView.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ReportCellExpandedView : UITableViewCell {
	UILabel *label;
	UILabel *labelTwo;
}
@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelTwo;
@end
