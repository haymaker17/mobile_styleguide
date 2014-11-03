//
//  SettingsBaseCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SettingsBaseCell : UITableViewCell {
    UILabel         *lblHeading, *lblSubheading, *lblRefundable;
    UISwitch        *switchView;
    NSString        *value, *rowKey;
    NSMutableDictionary *dictRowData;
}

@property (strong, nonatomic) IBOutlet UILabel				*lblHeading;
@property (strong, nonatomic) IBOutlet UILabel				*lblRefundable;
@property (strong, nonatomic) IBOutlet UILabel				*lblSubheading;
@property (strong, nonatomic) IBOutlet UISwitch             *switchView;
@property (strong, nonatomic) IBOutlet UILabel              *lblTravelPoints;
@property (strong, nonatomic) NSString                      *value;
@property (strong, nonatomic) NSString                      *rowKey;
@property (strong, nonatomic) NSMutableDictionary           *dictRowData;

-(IBAction) switchChanged:(id)sender;
@end
