//
//  ReceiptManSelectViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import	"UnifiedImagePicker.h"

@interface ReceiptManSelectViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource,UnifiedImagePickerDelegate>
{
	UITableView				*tableView;
	NSMutableArray			*tableData, *sectionData;

}

@property (retain, nonatomic) IBOutlet UITableView	*tableView;
@property (nonatomic, retain) NSMutableArray		*tableData;
@property (nonatomic, retain) NSMutableArray		*sectionData;

@end
