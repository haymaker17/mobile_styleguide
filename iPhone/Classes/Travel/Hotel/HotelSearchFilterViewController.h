//
//  HotelSearchFilterViewController.h
//  ConcurMobile
//
//  Created by Ray Chi on 9/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface HotelSearchFilterViewController : MobileViewController

@property (strong, nonatomic) IBOutlet UITextField *searchTextField;
@property (strong, nonatomic) IBOutlet UIView *mainView;


// A dictionary to store the last time's choise
@property (nonatomic,strong) NSMutableDictionary *selectedIndexDict;
@property (nonatomic,strong) void(^FilterTracking)(NSMutableDictionary *dict);

- (IBAction)btnReset_Click:(id)sender;
- (IBAction)btnDone_Click:(id)sender;


@end
