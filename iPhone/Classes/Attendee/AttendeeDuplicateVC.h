//
//  AttendeeDuplicateVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "AttendeeData.h"
#import "AttendeeEditorDelegate.h"

@interface AttendeeDuplicateVC : MobileViewController <UITableViewDataSource, UITableViewDelegate>
{
    id<AttendeeEditorDelegate>			__weak _editorDelegate;

    NSArray                             *duplicates;
    AttendeeData                        *attendee;
    UIBarButtonItem                     *btnSelect;
    AttendeeData                        *selectedAttendee;
    
    UITableView                         *tableList;
}

@property (strong, nonatomic) NSArray           *duplicates;
@property (strong, nonatomic) AttendeeData      *attendee;
@property (strong, nonatomic) AttendeeData      *selectedAttendee;
@property (weak, nonatomic) id<AttendeeEditorDelegate>		editorDelegate;
@property (strong, nonatomic) UIBarButtonItem   *btnSelect;

@property (strong, nonatomic) UITableView       *tableList;

-(void)makeSelectButton:(BOOL)active;
-(IBAction)buttonSelectPressed:(id)sender;
@end
