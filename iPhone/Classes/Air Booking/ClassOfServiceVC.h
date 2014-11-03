//
//  ClassOfServiceVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "BookingCellData.h"

@interface ClassOfServiceVC : MobileViewController <UITableViewDelegate, UITableViewDataSource>
{
	UITableView		*tableList;
    NSMutableArray          *aClass;
    NSMutableDictionary     *dictClass;
    int selectedRowIndex;
    BookingCellData         *bcd;
}

@property (strong, nonatomic) BookingCellData         *bcd;
@property int selectedRowIndex;
@property (strong, nonatomic) NSMutableArray          *aClass;
@property (strong, nonatomic) NSMutableDictionary     *dictClass;
@property (strong, nonatomic) IBOutlet UITableView		*tableList;
-(void)scrollToSelectedRow;
-(void)findSelected:(NSString *)key;
@end
