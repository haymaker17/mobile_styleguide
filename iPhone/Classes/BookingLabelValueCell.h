//
//  BookingLabelValueCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/26/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface BookingLabelValueCell : UITableViewCell
{
	UILabel		*labelHead;
	UILabel		*labelLabel;
	UILabel		*labelValue;
}

@property (nonatomic, strong) IBOutlet UILabel	*labelHead;
@property (nonatomic, strong) IBOutlet UILabel	*labelLabel;
@property (nonatomic, strong) IBOutlet UILabel	*labelValue;

+(BookingLabelValueCell*)makeHeaderCell:(UITableView*)tableView owner:(id)owner header:(NSString*)headerText;
+(BookingLabelValueCell*)makeCell:(UITableView*)tableView owner:(id)owner label:(NSString*)labelText value:(NSString*)valueText;
+(BookingLabelValueCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner;

@end
