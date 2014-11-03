//
//  AttendeeFullSearchResultsVC.h
//  ConcurMobile
//
//  Created by yiwen on 10/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "AttendeeSearchDelegate.h"
#import "AttendeeEntryEditViewController.h"
#import "AttendeeEditorDelegate.h"

@interface AttendeeFullSearchResultsVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, AttendeeEditorDelegate>
{
    id<AttendeeSearchDelegate>	__weak _delegate;
    
	UITableView					*tableList;
    
    NSArray						*searchResults;
    NSMutableArray              *atnColumns;
    
    NSMutableDictionary         *selectedAtnDict;   // Selected attendees, keyed by atnKey
    BOOL                        inAddAttendeeMode;  // Y - in select/add mode; N - in drill mode
    
    NSMutableDictionary         *externalAttendees;
    NSArray                     *entryFields;
    
}

@property (weak, nonatomic) id<AttendeeSearchDelegate>	delegate;
@property (strong, nonatomic) NSArray						*searchResults;
@property (strong, nonatomic) NSMutableArray                *atnColumns;
@property (strong, nonatomic) IBOutlet UITableView			*tableList;
@property (strong, nonatomic) NSMutableDictionary           *selectedAtnDict;
@property (strong, nonatomic) NSMutableDictionary           *externalAttendees;
@property BOOL                                              inAddAttendeeMode;
@property (strong, nonatomic) NSArray                       *entryFields;

- (void)setSeedData:(id<AttendeeSearchDelegate>)del searchResults:(NSArray*) attendees columns:(NSArray*) cols entryForm:(NSArray*)fields;
- (NSString*) titleForNoDataView;
- (NSString*) imageForNoDataView;
- (BOOL) canEdit;

@end
