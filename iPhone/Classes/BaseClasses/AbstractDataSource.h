//
//  AbstractDataSource.h
//  Concur Mobile
//
//  Created by Pavan Adavi on 6/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//
/*
 * Abstract class
 * =========== This class must be inherited =======
 * Data source for all data sources.
 * One data sources for each section in the view. 
 *
 */


#import <Foundation/Foundation.h>
#import "AbstractDataSourceDelegate.h"

typedef NS_ENUM(NSUInteger, DataSourceState) {
    kDataInitSearch,
    kDataLoading,
    kDataLoadComplete,
    kDataLoadPaused,
    kDataLoadResume,
    kDataLoadError,
};


@interface AbstractDataSource : NSObject

/* The state machine tracking for status of datasource.
 */
@property DataSourceState dataSourceState;

/* The keyPath on the fetched objects used to determine the section they belong to.
 */
@property (nonatomic, readonly) NSString *sectionNameKeyPath;


/* Delegate that is notified when the result set changes.
 */
@property(nonatomic, assign) id<AbstractDataSourceDelegate> delegate;

// ====== Initializer ============

/// Load the content of this data source.
- (void)loadContent;

/// Signal that the datasource SHOULD reload its content
- (void)setNeedsLoadContent;


/// Reset the content and loading state.
- (void)resetContent NS_REQUIRES_SUPER;

// ========= Accessors for items ===========


/// The items represented by this data source.
@property (nonatomic, copy) NSArray *items;

/// Set the items with optional animation. By default, setting the items is not animated.
- (void)setItems:(NSArray *)items;

/// The title of this data source. This value is used to populate section headers and the segmented control tab.
@property (nonatomic, copy) NSString *title;

/// The number of sections in this data source.
@property (nonatomic, readonly) NSInteger numberOfSections;

/// Find the data source for the given section. Default implementation returns self.
- (AbstractDataSource *)dataSourceForSectionAtIndex:(NSInteger)sectionIndex;

/// Find the item at the specified index path.
- (id)itemAtIndexPath:(NSIndexPath *)indexPath;

/// Find the index paths of the specified item in the data source. An item may appear more than once in a given data source.
- (NSArray*)indexPathsForItem:(id)item;

/// Remove an item from the data source. This method should only be called as the result of a user action, such as tapping the "Delete" button in a swipe-to-delete gesture. Automatic removal of items due to outside changes should instead be handled by the data source itself — not the controller. Data sources must implement this to support swipe-to-delete.
- (void)removeItemAtIndexPath:(NSIndexPath *)indexPath;

/// Add an item from the data source. Ideally this is not required by base class however a inheriting class can implement if required
-(void)insertItemAtIndexPath:(id)item  indexPath:(NSIndexPath *)indexPath;

/* ========================================================*/
/* =========== CONFIGURING SECTION INFORMATION ============*/
/* ========================================================*/
/*	These are meant to be optionally overridden by inheriting class.
 */

/* Returns the corresponding section index entry for a given section name.
 Default implementation returns the capitalized first letter of the section name.
 Developers that need different behavior can implement the delegate method - (NSString *)dataSource:(AbstractDataSource *)dataSource sectionIndexTitleForSectionName:(NSString *)sectionName;
 Only needed if a section index is used.
 */
- (NSString *)sectionIndexTitleForSectionName:(NSString *)sectionName;

/* Returns the array of section index titles.
 It's expected that developers call this method when implementing UITableViewDataSource's
 - (NSArray *)sectionIndexTitlesForTableView:(UITableView *)tableView
 
 The default implementation returns the array created by calling sectionIndexTitleForSectionName: on all the known sections.
 Developers should override this method if they wish to return a different array for the section index.
 Only needed if a section index is used.
 */
@property (nonatomic, readonly) NSArray *sectionIndexTitles;

/* ========================================================*/
/* =========== QUERYING SECTION INFORMATION ===============*/
/* ========================================================*/

/* Returns an array of objects that implement the NSFetchedResultsSectionInfo protocol.
 It's expected that developers use the returned array when implementing the following methods of the UITableViewDataSource protocol
 
 - (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView;
 - (NSInteger)tableView:(UITableView *)table numberOfRowsInSection:(NSInteger)section;
 - (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section;
 
 */
@property (nonatomic, readonly) NSArray *sections;

/* Returns the section number for a given section title and index in the section index.
 It's expected that developers call this method when executing UITableViewDataSource's
 - (NSInteger)tableView:(UITableView *)tableView sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)index;
 */
- (NSInteger)sectionForSectionIndexTitle:(NSString *)title atIndex:(NSInteger)sectionIndex;



@end
