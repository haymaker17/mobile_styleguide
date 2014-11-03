//
//  AllowanceMealProvidedTableViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 3/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AllowanceMealProvidedTableViewController.h"
#import "FixedAllowanceCell.h"

@interface AllowanceMealProvidedTableViewController ()

@end

@implementation AllowanceMealProvidedTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    if(self.mealName != nil)
    {
        self.title = self.mealName;
    }
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

//- (void)viewWillDisappear:(BOOL)animated {
//    // Need to do this because the default back button cant be attached to exit from IB
//    // You connect the viewcontroller to the exit to create the segue, then call it from code.
//    [self performSegueWithIdentifier:@"unwindToAllowanceAdjustmentFromMealPicker" sender:self];
//}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
#warning Potentially incomplete method implementation.
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
#warning Incomplete method implementation.
    // Return the number of rows in the section.
    //Add a switch to only return the first two rows

    return 3;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    FixedAllowanceCell *cell = (FixedAllowanceCell *)[tableView dequeueReusableCellWithIdentifier:@"MealStatus" forIndexPath:indexPath];
    if(indexPath.row == 0)
    {
        cell.mealProvided = @"NPR";
    }
    else if(indexPath.row == 1)
    {
        cell.mealProvided = @"PRO";
    }
    else if(indexPath.row == 2)
    {
        cell.mealProvided = @"TAX";
    }

    cell.mealProvidedStatusLabel.text = [FixedAllowanceCell getMealProvidedValueLabel:cell.mealProvided];
    
    // Configure the cell...
    if([cell.mealProvided isEqualToString:self.mealProvided])
    {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    }
    
    return cell;
}



/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    FixedAllowanceCell *cell = (FixedAllowanceCell *)sender;
    self.mealProvided = cell.mealProvided;
}


@end
