//
//  ListViewController.m
//  ConcurMobile
//
//  Created by Shifan Wu on 7/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ListViewController.h"
#import "ListItem.h"

@interface ListViewController ()

@end

@implementation ListViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    UINavigationBar *naviBar = [[UINavigationBar alloc] initWithFrame:CGRectMake(0, 20, 320, 44)];
    [self.view addSubview:naviBar];
    
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"] style:UIBarButtonItemStyleBordered target:self action:@selector(buttonClosePressed)];
    UINavigationItem *naviBarItems = [[UINavigationItem alloc] initWithTitle:[Localizer getLocalizedText:@"Sort"]];
    
    naviBarItems.leftBarButtonItem = btnClose;
    naviBar.items = @[naviBarItems];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma - mark UITableViewDataSource
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.dataSourceArray count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"defaultCell";
    ListItem *oneListItem = self.dataSourceArray[indexPath.row];
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"defaultCell"];
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:CellIdentifier];
    }
    
    cell.textLabel.text = oneListItem.liName;
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    
    if (self.defaultSelectedIdxPath != nil && indexPath.row == self.defaultSelectedIdxPath.row)
    {
        [cell setAccessoryType:UITableViewCellAccessoryCheckmark];
    }
    else
        [cell setAccessoryType:UITableViewCellAccessoryNone];
    
    return cell;
}

#pragma - mark UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row != self.defaultSelectedIdxPath.row)
    {
        // defaultSelectedIdxPath is NSIndexPath
        NSIndexPath *previousSelection = self.defaultSelectedIdxPath;
        NSArray *array = nil;
        if (nil != previousSelection)
            array = [NSArray arrayWithObjects:previousSelection, indexPath, nil];
        else
            array = [NSArray arrayWithObject:indexPath];
        
        self.defaultSelectedIdxPath = indexPath;
        
        [tableView reloadRowsAtIndexPaths:array withRowAnimation: UITableViewRowAnimationNone];
    }
    
    ListItem *oneListItem = self.dataSourceArray[indexPath.row];
    [self dismissViewControllerAnimated:YES completion:^{
        [self.delegate optionSelectedAtIndex:indexPath.row withIdentifier:oneListItem.liName];
    }];
}

- (void)buttonClosePressed
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
