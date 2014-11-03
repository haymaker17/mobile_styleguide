//
//  ChooseSeatViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseSeatViewController.h"
#import "Localizer.h"

@interface ChooseSeatViewController ()

@end

@implementation ChooseSeatViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
    }
    
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - UITableViewDataSource

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView
                             dequeueReusableCellWithIdentifier:@"SeatTypeCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"SeatTypeCell"];
    }
    
    NSString *seatType;
    
    switch ([indexPath row]) {
        case 0:
            seatType = [Localizer getLocalizedText:@"reserved"];
            break;
        case 1:
            seatType = [Localizer getLocalizedText:@"unreserved"];
            break;
        case 2:
            seatType = [Localizer getLocalizedText:@"green"];
            break;
        default:
            break;
    }
    
    cell.textLabel.text = seatType;
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 3;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *seatType;
    
    switch ([indexPath row]) {
        case 0:
            seatType = [Localizer getLocalizedText:@"reserved"];
            break;
        case 1:
            seatType = [Localizer getLocalizedText:@"unreserved"];
            break;
        case 2:
            seatType = [Localizer getLocalizedText:@"green"];
            break;
        default:
            break;
    }
    
    if (seatType != nil) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"SeatType"
                                                            object:[NSNumber numberWithInt:[indexPath row]]];
    }
    
    [[self navigationController] popViewControllerAnimated:YES];
}

@end
