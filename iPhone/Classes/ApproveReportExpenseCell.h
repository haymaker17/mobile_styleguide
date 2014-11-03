//
//  ApproveReportExpenseCell.h
//  ConcurMobile
//
//  Created by Yuri on 2/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class RootViewController;

@interface ApproveReportExpenseCell : UITableViewCell {
    
	UILabel *labelExpense;
	UILabel *labelTotal;
	
	UILabel *labelName;
	UILabel *labelDate;
    
	NSMutableArray* listIcon;
}

@property (nonatomic, retain) IBOutlet UILabel *labelExpense;
@property (nonatomic, retain) IBOutlet UILabel *labelTotal;

@property (nonatomic, retain) IBOutlet UILabel *labelName;
@property (nonatomic, retain) IBOutlet UILabel *labelDate;

@property (nonatomic, retain) NSMutableArray* listIcon;

- (id) initWithData:(NSDictionary*) entryData;

//- (NSString *) reuseIdentifier;

-(NSString*) vendorForRowData:(NSDictionary *) rowData;
-(NSString*) locationForRowData:(NSDictionary *)rowData;

@end
