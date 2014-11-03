//
//  ReportCellView.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/6/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ReportCellView : UITableViewCell {
	UILabel *label;
	UILabel *labelTwo;
}

@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelTwo;

@end
